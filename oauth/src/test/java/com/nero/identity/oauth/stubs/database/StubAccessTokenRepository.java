package com.nero.identity.oauth.stubs.database;

import java.util.HashMap;
import java.util.Map;

import com.nero.identity.oauth.data.AccessToken;
import com.nero.identity.oauth.data.repositories.AccessTokenRepository;

public class StubAccessTokenRepository implements AccessTokenRepository {
	private Map<Long, AccessToken> database;
	private Long id = 1L;
	
	public StubAccessTokenRepository() {
		this.database = new HashMap<>();
	}

	@Override
	public AccessToken getToken(String token) {
		for(Map.Entry<Long, AccessToken> entry : database.entrySet()) {
			AccessToken dbToken = entry.getValue();
			if(dbToken.getToken().equals(token)) {
				return dbToken;
			}
		}
		
		return null;
	}

	@Override
	public Boolean deleteToken(Long tokenId) {
		if(database.remove(tokenId) != null)
			return true;
		
		return false;
	}

	@Override
	public AccessToken save(AccessToken token) {
		token.setId(id);
		id++;
		database.put(token.getId(), token);
		return token;
	}

}
