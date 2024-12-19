package com.nero.identity.oauth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import com.nero.identity.oauth.data.AccessToken;
import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.RefreshToken;
import com.nero.identity.oauth.data.Token;
import com.nero.identity.oauth.data.TokenRequest;
import com.nero.identity.oauth.data.repositories.AuthCodeRepository;
import com.nero.identity.oauth.data.repositories.ClientRepository;
import com.nero.identity.oauth.service.TokenService;
import com.nero.identity.oauth.stubs.database.StubAccessTokenRepository;
import com.nero.identity.oauth.stubs.database.StubAuthCodeRepository;
import com.nero.identity.oauth.stubs.database.StubClientRepository;
import com.nero.identity.oauth.stubs.database.StubRefreshTokenRepository;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenServiceTest {
	private static TokenService tokenService;
	private static ClientRepository clientRepo;
	private static AuthCodeRepository authCodeRepo;
	private static UUID clientId = UUID.randomUUID();
	private static String authorizationCode = "test code";
	private static String clientSecret = "secret";
	
	@BeforeAll
	public static void setUp() {
		clientRepo = new StubClientRepository();
		authCodeRepo = new StubAuthCodeRepository();
		tokenService = new TokenService(authCodeRepo, 
				new StubAccessTokenRepository(), new StubRefreshTokenRepository(), clientRepo);
	}
	
	@BeforeEach
	public void beforeEachTest() {
		Client client = new Client();
		client.setClientId(clientId);
		client.setClientSecret(clientSecret);
		client.setClientName("testClient");
		client.setId(1L);
		client.setRedirectUri("www.fake.com");
		client.setScope("read write delete");
		clientRepo.saveClient(client);
		
		AuthCode authCode = new AuthCode();
		authCode.setClientId(clientId.toString());
		authCode.setAuthorizationCode(authorizationCode);
		authCodeRepo.saveCode(authCode);
	}
	
	@Test
	public void testGenerateAuthorizationCode() {
		AuthCode authCode = tokenService.generateAuthorizationCode(clientId.toString());
		assertNotNull(authCode, "Returned Authorization Code shouldn't be null");
		assertEquals(authCode.getClientId(), clientId.toString());
	}
	
	@Test
	public void testHandleRefreshTokenForBadToken() {
		Token returnedToken = tokenService.handleRefreshToken("this token doesn't exist");
		assertNull(returnedToken);
	}
	
	@Test
	public void testParseTokenRequestWithClientIdInAuthHeader() {
		String credentials = clientId + ":" + clientSecret;
		String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
		String authHeader = "Basic " + encodedCredentials;
		Map<String, String> request = new HashMap<>();
		request.put("grant_type", "test_grant");
		request.put("code", "test_code");
		
		TokenRequest parsedRequest = tokenService.parseTokenRequest(request, authHeader);
		assertNotNull(parsedRequest);
		assertFalse(parsedRequest.isError());
		assertEquals(parsedRequest.getClientId(), clientId.toString());
		assertEquals(parsedRequest.getClientSecret(), clientSecret);
		assertEquals(parsedRequest.getGrantType(), "test_grant");
		assertEquals(parsedRequest.getCode(), "test_code");
	}
	
	@Test
	public void testParseTokenRequestWithDuplicateAuthLocations() {
		String credentials = clientId + ":" + clientSecret;
		String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
		String authHeader = "Basic " + encodedCredentials;
		Map<String, String> request = new HashMap<>();
		request.put("grant_type", "test_grant");
		request.put("code", "test_code");
		request.put("client_id", clientId.toString());
		request.put("client_secret", clientSecret);
		TokenRequest parsedRequest = tokenService.parseTokenRequest(request, authHeader);
		assertNotNull(parsedRequest);
		assertTrue(parsedRequest.isError());
		assertEquals(parsedRequest.getErrorMessage(), "invalid_client");
	}
	
	@Test
	public void testParseTokenRequestWithNoGrantType() {
		String credentials = clientId + ":" + clientSecret;
		String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
		String authHeader = "Basic " + encodedCredentials;
		Map<String, String> request = new HashMap<>();
		request.put("code", "test_code");
		TokenRequest parsedRequest = tokenService.parseTokenRequest(request, authHeader);
		assertNotNull(parsedRequest);
		assertTrue(parsedRequest.isError());
		assertEquals(parsedRequest.getErrorMessage(), "no_grant_type");
	}
	
	@Test
	public void testParseTokenRequestWithNoCredentials() {
		Map<String, String> request = new HashMap<>();
		request.put("code", "test_code");
		request.put("grant_type", "test_grant");
		String authHeader = "";
		TokenRequest parsedRequest = tokenService.parseTokenRequest(request, authHeader);
		assertNotNull(parsedRequest);
		assertTrue(parsedRequest.isError());
		assertEquals(parsedRequest.getErrorMessage(), "invalid_client");
	}
	
	@Test
	public void testHandleAuthorizationCode(TestReporter testReporter) {
		String scope = "read write";
		Token token = tokenService.handleAuthorizationCode(authorizationCode, clientId.toString(), scope);
		assertNotNull(token);
		AccessToken accessToken = token.getAccessToken();
		
		assertNotNull(accessToken);
		assertEquals(accessToken.getClientId(), clientId.toString());
		assertEquals(accessToken.getScope(), scope);
		assertNotNull(accessToken.getExpirationTime());
		assertTrue(accessToken.getExpirationTime().after(Date.from(Instant.now())), "Expiration time for access token should be after current time");
		testReporter.publishEntry(accessToken.getId().toString());
		testReporter.publishEntry(accessToken.toString());
		RefreshToken refreshToken = token.getRefreshToken();
		assertNotNull(refreshToken);
		assertNotNull(refreshToken.getExpirationTime());
		assertEquals(refreshToken.getScope(), scope);
		assertTrue(refreshToken.getExpirationTime().after(Date.from(Instant.now())), "Expiration time for refresh token should be after current time");
		testReporter.publishEntry(refreshToken.toString());
		
		AuthCode oldCode = authCodeRepo.verifyCode(authorizationCode);
		
		assertNull(oldCode, "Authorization Codes should be deleted after use");
	}
	
	@Test
	public void testHandleAuthorizationCodeWithInvalidCode() {
		String scope = "read write";
		Token token = tokenService.handleAuthorizationCode("bad code", clientId.toString(), scope);
		assertNull(token, "Service should return no token when the authorization code doesn't exist in database");
	}
	
	@Test
	public void testHandleAuthorizationCodeWithInvalidClient() {
		String scope = "read write";
		Token token = tokenService.handleAuthorizationCode(authorizationCode, "badClient", scope);
		assertNull(token, "Service should return no token when an invalid client is used");
	}
}
