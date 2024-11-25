package com.nero.identity.oauth.data.repositories;

import com.nero.identity.oauth.data.AccessToken;

public interface AccessTokenRepository {
	AccessToken getToken(String token);
	Boolean deleteToken(Long tokenId);
	AccessToken save(AccessToken token);
}
