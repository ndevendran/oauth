package com.nero.identity.oauth.data.repositories;

import com.nero.identity.oauth.data.AuthCode;

public interface AuthCodeRepository {
	boolean verifyCode(String authorizationCode);
	String saveCode(AuthCode authCode);
}
