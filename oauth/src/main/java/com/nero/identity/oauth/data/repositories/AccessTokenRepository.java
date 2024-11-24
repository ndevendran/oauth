package com.nero.identity.oauth.data.repositories;

import com.nero.identity.oauth.data.AccessToken;

public interface AccessTokenRepository {
	AccessToken getToken(String token);
	Boolean deleteToken(String token);
	AccessToken save(AccessToken token);
}
