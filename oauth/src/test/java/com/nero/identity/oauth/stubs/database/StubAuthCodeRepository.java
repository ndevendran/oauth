package com.nero.identity.oauth.stubs.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.repositories.AuthCodeRepository;

public class StubAuthCodeRepository implements AuthCodeRepository {
	
	private Map<String, AuthCode> database;
	
	public StubAuthCodeRepository() {
		this.database = new HashMap<>();
	}

//	@Override
//	public AuthCode verifyCode(String authorizationCode) {
//		return database.get(authorizationCode);
//	}
//
//	@Override
//	public boolean deleteCode(String authorizationCode) {
//		if(database.remove(authorizationCode) != null)
//			return true;
//		
//		return false;
//	}
//
//	@Override
//	public AuthCode saveCode(AuthCode authCode) {
//		database.put(authCode.getAuthorizationCode(), authCode);
//		return authCode;
//	}

	@Override
	public <S extends AuthCode> S save(S entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends AuthCode> Iterable<S> saveAll(Iterable<S> entities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<AuthCode> findById(Long id) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public boolean existsById(Long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<AuthCode> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<AuthCode> findAllById(Iterable<Long> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void deleteById(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(AuthCode entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAllById(Iterable<? extends Long> ids) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll(Iterable<? extends AuthCode> entities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AuthCode> findByAuthorizationCode(String authorizationCode) {
		// TODO Auto-generated method stub
		return null;
	}

}
