package com.nero.identity.oauth.stubs.database;

import java.util.HashMap;
import java.util.Map;

import com.nero.identity.oauth.data.RefreshToken;
import com.nero.identity.oauth.data.repositories.RefreshTokenRepository;

public class StubRefreshTokenRepository implements RefreshTokenRepository {
	private Map<Long, RefreshToken> database;
	
	public StubRefreshTokenRepository() {
		this.database = new HashMap<>();
	}

	@Override
	public RefreshToken getRefreshToken(String refreshToken) {
		for (Map.Entry<Long, RefreshToken> entry : database.entrySet()) {
			RefreshToken dbToken = entry.getValue();
			if(dbToken.getToken().equals(refreshToken)) {
				return dbToken;
			}
		}
		
		return null;
	}

	@Override
	public RefreshToken saveRefreshToken(RefreshToken refreshToken) {
		database.put(refreshToken.getId(), refreshToken);
		return refreshToken;
	}

	@Override
	public Long getAccessTokenId(Long refreshTokenId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long updateRefreshTokenWithNewAccessToken(Long refreshTokenId, Long accessTokenId) {
		// TODO Auto-generated method stub
		return null;
	}

}
