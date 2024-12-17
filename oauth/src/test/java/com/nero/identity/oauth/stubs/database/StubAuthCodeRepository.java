package com.nero.identity.oauth.stubs.database;

import java.util.HashMap;
import java.util.Map;

import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.repositories.AuthCodeRepository;

public class StubAuthCodeRepository implements AuthCodeRepository {
	
	private Map<String, AuthCode> database;
	
	public StubAuthCodeRepository() {
		this.database = new HashMap<>();
	}

	@Override
	public AuthCode verifyCode(String authorizationCode) {
		return database.get(authorizationCode);
	}

	@Override
	public boolean deleteCode(String authorizationCode) {
		if(database.remove(authorizationCode) != null)
			return true;
		
		return false;
	}

	@Override
	public AuthCode saveCode(AuthCode authCode) {
		database.put(authCode.getAuthorizationCode(), authCode);
		return authCode;
	}

}
