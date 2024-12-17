package com.nero.identity.oauth.data.repositories;

import com.nero.identity.oauth.data.AuthCode;

public interface AuthCodeRepository {
	AuthCode verifyCode(String authorizationCode);
	boolean deleteCode(String authorizationCode);
	AuthCode saveCode(AuthCode authCode);
}
