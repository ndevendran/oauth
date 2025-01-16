package com.nero.identity.oauth.repositories;



import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.nero.identity.oauth.data.AccessToken;
import com.nero.identity.oauth.data.repositories.AccessTokenRepository;

@SpringBootTest()
public class AccessTokenRepositoryTest {
	@Autowired
	AccessTokenRepository accessTokenRepo;
	
	@Test
	public void testSaveAndGetAccessToken() {
		AccessToken accessToken = new AccessToken();
		accessToken.setToken("token");
		accessToken.setClientId("clientId");
		accessToken.setScope("scope");
		java.time.LocalDate expirationTime = java.time.LocalDate.now();
		accessToken.setExpirationTime(java.sql.Date.valueOf(expirationTime));
		AccessToken savedToken = accessTokenRepo.save(accessToken);
		assertNotNull(savedToken);
		assertNotNull(savedToken.getId());
	}
}
