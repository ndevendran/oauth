package com.nero.identity.oauth.service;

import java.sql.Date;
import java.time.Instant;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nero.identity.oauth.data.AccessToken;
import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.RefreshToken;
import com.nero.identity.oauth.data.Token;
import com.nero.identity.oauth.data.repositories.AccessTokenRepository;
import com.nero.identity.oauth.data.repositories.AuthCodeRepository;
import com.nero.identity.oauth.data.repositories.RefreshTokenRepository;

@Service
public class TokenService {
	private AuthCodeRepository codeRepo;
	private AccessTokenRepository accessTokenRepo;
	private RefreshTokenRepository refreshTokenRepo;

	@Autowired
	public TokenService(AuthCodeRepository codeRepo, AccessTokenRepository accessTokenRepo, RefreshTokenRepository refreshTokenRepo) {
		this.codeRepo = codeRepo;
		this.accessTokenRepo = accessTokenRepo;
		this.refreshTokenRepo = refreshTokenRepo;
	}
	
	public Token handleAuthorizationCode(String code, String clientId) {
		AuthCode storedCode = this.codeRepo.verifyCode(code);
		this.codeRepo.deleteCode(code);
		
		if(storedCode.getClientId().equals(clientId)) {
			String token = UUID.randomUUID().toString();
			while(accessTokenRepo.getToken(token) != null) {
				token = UUID.randomUUID().toString();
			}
						
			AccessToken accessToken = new AccessToken();
			accessToken.setToken(token);
			accessToken.setClientId(clientId);


			Date expirationTime = new Date(Date.from(Instant.now().plusSeconds(86400)).getTime());
			accessToken.setExpirationTime(expirationTime);
			accessToken = accessTokenRepo.save(accessToken);
			
			//generate refresh token
			token = UUID.randomUUID().toString();
			while(refreshTokenRepo.getRefreshToken(token) != null) {
				token = UUID.randomUUID().toString();
			}
			expirationTime = new Date(Date.from(Instant.now().plusSeconds(604800)).getTime());
			RefreshToken refreshToken = new RefreshToken();
			refreshToken.setToken(token);
			refreshToken.setClientId(clientId);
			refreshToken.setExpirationTime(expirationTime);
			
			refreshToken = refreshTokenRepo.saveRefreshToken(refreshToken);
			refreshTokenRepo.updateRefreshTokenWithNewAccessToken(refreshToken.getId(), accessToken.getId());
			
			Token dbToken = new Token();
			dbToken.setAccessToken(accessToken);
			dbToken.setRefreshToken(refreshToken);
			
			return dbToken;
		} else {
			return null;
		}
	}
	
	public Token handleRefreshToken(String refreshToken) {
		RefreshToken dbRefreshToken = refreshTokenRepo.getRefreshToken(refreshToken);
		
		//check refresh token exists
		if(dbRefreshToken == null) {
			return null;
		}
		
		//check expiration date
		if(dbRefreshToken.getExpirationTime().before(new java.util.Date()))
		{
			return null;
		}
		
		//if everything is good generate a new access token
		String token = UUID.randomUUID().toString();
		while(accessTokenRepo.getToken(token) != null) {
			token = UUID.randomUUID().toString();
		}
					
		AccessToken accessToken = new AccessToken();
		accessToken.setToken(token);
		accessToken.setClientId(dbRefreshToken.getClientId());


		Date expirationTime = new Date(Date.from(Instant.now().plusSeconds(86400)).getTime());
		accessToken.setExpirationTime(expirationTime);
		accessToken = accessTokenRepo.save(accessToken);
		
		//bind access token to refresh token
		refreshTokenRepo.updateRefreshTokenWithNewAccessToken(dbRefreshToken.getId(), accessToken.getId());
		
		//create the new token object and return it
		Token tokenResponse = new Token();
		tokenResponse.setAccessToken(accessToken);
		tokenResponse.setRefreshToken(dbRefreshToken);
		
		return tokenResponse;
	}
	
	public String handleAuthorizationCode(String clientId) {
		UUID authorizationCode = UUID.randomUUID();
    	
    	while(codeRepo.verifyCode(authorizationCode.toString()) != null) {
    		authorizationCode = UUID.randomUUID();
    	}
    	
    	
    	AuthCode code = new AuthCode();
    	code.setAuthorizationCode(authorizationCode.toString());
    	code.setClientId(clientId);
    	
    	return codeRepo.saveCode(code);
	}
}
