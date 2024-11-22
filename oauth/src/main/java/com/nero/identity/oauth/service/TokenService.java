package com.nero.identity.oauth.service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.Token;
import com.nero.identity.oauth.data.repositories.AuthCodeRepository;
import com.nero.identity.oauth.data.repositories.TokenRepository;

@Service
public class TokenService {
	private AuthCodeRepository codeRepo;
	private TokenRepository tokenRepo;

	@Autowired
	public TokenService(AuthCodeRepository codeRepo, TokenRepository tokenRepo) {
		this.codeRepo = codeRepo;
		this.tokenRepo = tokenRepo;
	}
	
	public Token handleAuthorizationCode(String code, String clientId) {
		AuthCode storedCode = this.codeRepo.verifyCode(code);
		this.codeRepo.deleteCode(code);
		
		if(storedCode.getClientId().equals(clientId)) {
			String token = UUID.randomUUID().toString();
			while(tokenRepo.getToken(token) != null) {
				token = UUID.randomUUID().toString();
			}
			Token dbToken = new Token();
			dbToken.setToken(token);
			dbToken.setClientId(clientId);

			Date expirationTime = Date.from(Instant.now().plusSeconds(604800));
			dbToken.setExpirationTime(expirationTime);
			tokenRepo.save(dbToken);
			return dbToken;
		} else {
			return null;
		}
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
