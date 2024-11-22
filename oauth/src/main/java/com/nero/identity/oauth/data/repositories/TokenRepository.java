package com.nero.identity.oauth.data.repositories;

import com.nero.identity.oauth.data.Token;

public interface TokenRepository {
	Token getToken(String token);
	Boolean deleteToken(String token);
	Token save(Token token);
}
