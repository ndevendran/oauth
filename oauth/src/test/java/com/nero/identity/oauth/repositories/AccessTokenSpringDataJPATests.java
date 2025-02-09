package com.nero.identity.oauth.repositories;

import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nero.identity.oauth.data.AccessToken;
import com.nero.identity.oauth.data.RefreshToken;
import com.nero.identity.oauth.data.repositories.AccessTokenRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccessTokenSpringDataJPATests {
	@Autowired
	AccessTokenRepository accessTokenRepo;
	String token;
	String refreshToken;
	String clientId;
	
	@BeforeAll
	void beforeAll() {
		clientId = "testClient";
		AccessToken accessToken = new AccessToken();
		token = UUID.randomUUID().toString();
		accessToken.setToken(token);
		Date expirationTime = new Date(Date.from(Instant.now().plusSeconds(86400)).getTime());
		accessToken.setExpirationTime(expirationTime);
		accessToken.setScope(null);
		RefreshToken refreshTokenObj = new RefreshToken();
		refreshToken = UUID.randomUUID().toString();
		refreshTokenObj.setToken(refreshToken);
		refreshTokenObj.setExpirationTime(new Date(Date.from(Instant.now().plusSeconds(86400)).getTime()));
		accessToken.setRefreshToken(refreshTokenObj);
		accessToken.setClientId(clientId);
		accessTokenRepo.save(accessToken);
	}
	
	@Test
	void testFindByToken() {
		AccessToken accessToken = accessTokenRepo.findByToken(token);
		assertNotNull(accessToken);
		assertEquals(accessToken.getToken(), token);
		assertEquals(accessToken.getRefreshToken().getToken(), refreshToken);
		assertEquals(accessToken.getClientId(), clientId);
	}
	
	@Test
	void testFindByRefreshToken() {
		AccessToken accessToken = accessTokenRepo.findByRefreshTokenToken(refreshToken);
		assertNotNull(accessToken);
		assertEquals(accessToken.getToken(), token);
		assertEquals(accessToken.getRefreshToken().getToken(), refreshToken);
		assertEquals(accessToken.getClientId(), clientId);
	}
	
}
