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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

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
	

	private static ClientRepository mockClientRepo;
	
	private static AuthCodeRepository mockAuthCodeRepo;
	
	private static UUID clientId = UUID.randomUUID();
	private static String authorizationCode = "test code";
	private static String clientSecret = "secret";
	
	@BeforeAll
	public static void setUp() {
		mockClientRepo = new StubClientRepository();
		mockAuthCodeRepo = new StubAuthCodeRepository();
		tokenService = new TokenService(mockAuthCodeRepo, 
				new StubAccessTokenRepository(), new StubRefreshTokenRepository(), mockClientRepo);
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
		mockClientRepo.saveClient(client);


		AuthCode authCode = new AuthCode();
		authCode.setClientId(clientId.toString());
		authCode.setAuthorizationCode(authorizationCode);
		mockAuthCodeRepo.saveCode(authCode);
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
		
		AuthCode oldCode = mockAuthCodeRepo.verifyCode(authorizationCode);
		
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
	
	@ParameterizedTest
	@CsvFileSource(resources="/test/parseTokenRequest.csv")
	void testParseTokenWithParameterizedArguements(boolean basicAuth, boolean bodyAuth, String grantType, String testCode,
			boolean isError, String errorMessage) {
		Map<String, String> request = new HashMap<>();
		String authHeader = "";
		if(basicAuth) {
			String credentials = clientId + ":" + clientSecret;
			String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
			authHeader = "Basic " + encodedCredentials;
		}
		if(bodyAuth) {
			request.put("client_id", clientId.toString());
			request.put("client_secret", clientSecret);
		}
		
		if(!grantType.equals("null")) {
			request.put("grant_type", grantType);
		}

		request.put("code", testCode);
		
		TokenRequest parsedRequest = tokenService.parseTokenRequest(request, authHeader);
		
		assertNotNull(parsedRequest);
		
		if(isError) {
			assertTrue(parsedRequest.isError());
			assertEquals(parsedRequest.getErrorMessage(), errorMessage);
		} else {
			assertFalse(parsedRequest.isError());
			assertEquals(parsedRequest.getClientId(), clientId.toString());
			assertEquals(parsedRequest.getClientSecret(), clientSecret);
			assertEquals(parsedRequest.getGrantType(), grantType);
			assertEquals(parsedRequest.getCode(), testCode);
		}

	}
}
