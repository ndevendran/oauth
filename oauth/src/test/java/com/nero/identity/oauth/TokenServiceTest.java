package com.nero.identity.oauth;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.Token;
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

public class TokenServiceTest {
	private static TokenService tokenService;
	private static ClientRepository clientRepo;
	private static AuthCodeRepository authCodeRepo;
	private static UUID clientId = UUID.randomUUID();
	private static String authorizationCode = "test code";
	
	@BeforeAll
	public static void setUp() {
		clientRepo = new StubClientRepository();
		authCodeRepo = new StubAuthCodeRepository();
		Client client = new Client();
		client.setClientId(clientId);
		client.setClientSecret("secret");
		client.setClientName("testClient");
		client.setId(1L);
		client.setRedirectUri("www.fake.com");
		client.setScope("read write delete");
		clientRepo.saveClient(client);
		
		AuthCode authCode = new AuthCode();
		authCode.setClientId(clientId.toString());
		authCode.setAuthorizationCode(authorizationCode);
		authCodeRepo.saveCode(authCode);
		tokenService = new TokenService(authCodeRepo, 
				new StubAccessTokenRepository(), new StubRefreshTokenRepository(), clientRepo);
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
	
	
}
