package com.nero.identity.oauth.service;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nero.identity.oauth.data.AccessToken;
import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.RefreshToken;
import com.nero.identity.oauth.data.Token;
import com.nero.identity.oauth.data.TokenRequest;
import com.nero.identity.oauth.data.repositories.AccessTokenRepository;
import com.nero.identity.oauth.data.repositories.AuthCodeRepository;
import com.nero.identity.oauth.data.repositories.ClientRepository;
import com.nero.identity.oauth.data.repositories.RefreshTokenRepository;

@Service
public class TokenService {
	private AuthCodeRepository codeRepo;
	private AccessTokenRepository accessTokenRepo;
	private RefreshTokenRepository refreshTokenRepo;
	private ClientRepository clientRepo;

	@Autowired
	public TokenService(AuthCodeRepository codeRepo, AccessTokenRepository accessTokenRepo, 
			RefreshTokenRepository refreshTokenRepo, ClientRepository clientRepo) {
		this.codeRepo = codeRepo;
		this.accessTokenRepo = accessTokenRepo;
		this.refreshTokenRepo = refreshTokenRepo;
		this.clientRepo = clientRepo;
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
		
		//get reference to old access token id before we change binding
		Long oldAccessTokenId = refreshTokenRepo.getAccessTokenId(dbRefreshToken.getId());
		
		//bind access token to refresh token
		refreshTokenRepo.updateRefreshTokenWithNewAccessToken(dbRefreshToken.getId(), accessToken.getId());
		
		//now we can delete old access token
		accessTokenRepo.deleteToken(oldAccessTokenId);
		
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
	
	public TokenRequest parseTokenRequest(Map<String, String> requestBody, String authHeader) {
		TokenRequest request = new TokenRequest();
		String clientId = null;
		String clientSecret = null;
    	if(authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring(6);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes, StandardCharsets.UTF_8);

            // Split into username and password
            String[] clientDetails = credentials.split(":", 2);
            clientId = clientDetails[0];
            clientSecret = clientDetails.length > 1 ? clientDetails[1] : "";
    	}
    	
    	String client_id = requestBody.get("client_id");
    	String client_secret = requestBody.get("client_secret");
    	String grant_type = requestBody.get("grant_type");
    	String code = requestBody.get("code");
    	
    	if(client_id == null && clientId == null) {
    		request.setError(true);
    		request.setErrorMessage("invalid_client");
    		return request;
    	}
    	
    	if(client_id != null && clientId != null) {
    		request.setError(true);
    		request.setErrorMessage("invalid_client");
    		return request;
    	}
    	
    	if(clientId == null) {
    		clientId = client_id;
    		clientSecret = client_secret;
    	}
    	
    	Client client = clientRepo.findClient(clientId);
    	if(client == null) {
    		request.setError(true);
    		request.setErrorMessage("invalid_client");
    		return request;
    	}
    	
    	if(!client.getClientSecret().equals(clientSecret)) {
    		request.setError(true);
    		request.setErrorMessage("invalid_client");
    		return request;
    	}
    	if(grant_type == null) {
    		request.setError(true);
    		request.setErrorMessage("no_grant_type");
    		return request;
    	}
    	
    	request.setClientId(clientId);
    	request.setClientSecret(clientSecret);
    	request.setGrantType(grant_type);
    	request.setCode(code);
    	
    	
    	return request;
	}
}
