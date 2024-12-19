package com.nero.identity.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.repositories.AuthCodeRepository;
import com.nero.identity.oauth.data.repositories.ClientRepository;
import com.nero.identity.oauth.service.ClientService;
import com.nero.identity.oauth.stubs.database.StubAuthCodeRepository;
import com.nero.identity.oauth.stubs.database.StubClientRepository;

public class ClientServiceTest {
	private static ClientService clientService;
	private static ClientRepository clientRepo;
	private static AuthCodeRepository authCodeRepo;
	private static UUID clientId = UUID.randomUUID();
	
	@BeforeAll
	public static void setUp() {
		clientRepo = new StubClientRepository();
		authCodeRepo = new StubAuthCodeRepository();
		clientService = new ClientService(clientRepo, authCodeRepo);
	}
	
	@Test
	public void testRegisterClient() {
		
	}
	
	@Test
	public void testGenerateAuthorizationCode() {
		AuthCode authCode = clientService.generateAuthorizationCode(clientId.toString());
		assertNotNull(authCode, "Returned Authorization Code shouldn't be null");
		assertEquals(authCode.getClientId(), clientId.toString());
	}
}
