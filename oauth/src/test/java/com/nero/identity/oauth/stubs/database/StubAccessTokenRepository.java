package com.nero.identity.oauth.stubs.database;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.nero.identity.oauth.data.AccessToken;
import com.nero.identity.oauth.data.repositories.AccessTokenRepository;

public class StubAccessTokenRepository implements AccessTokenRepository {
	private Map<Long, AccessToken> database;
	private Long id = 1L;
	
	public StubAccessTokenRepository() {
		this.database = new HashMap<>();
	}


//	public AccessToken getToken(String token) {
//		for(Map.Entry<Long, AccessToken> entry : database.entrySet()) {
//			AccessToken dbToken = entry.getValue();
//			if(dbToken.getToken().equals(token)) {
//				return dbToken;
//			}
//		}
//		
//		return null;
//	}
//
//	public Boolean deleteToken(Long tokenId) {
//		if(database.remove(tokenId) != null)
//			return true;
//		
//		return false;
//	}

	@Override
	public <S extends AccessToken> S save(S token) {
		token.setId(id);
		id++;
		database.put(token.getId(), token);
		return token;
	}

	@Override
	public <S extends AccessToken> Iterable<S> saveAll(Iterable<S> entities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<AccessToken> findById(Long id) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public boolean existsById(Long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterable<AccessToken> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<AccessToken> findAllById(Iterable<Long> ids) {
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
	public void delete(AccessToken entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAllById(Iterable<? extends Long> ids) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll(Iterable<? extends AccessToken> entities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AccessToken findByToken(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccessToken findByRefreshTokenToken(String refreshToken) {
		for(Map.Entry<Long, AccessToken> entry : database.entrySet()) {
			AccessToken dbToken = entry.getValue();
			if(dbToken.getRefreshToken().getToken().equals(refreshToken)) {
				return dbToken;
			}
		}
		
		return null;
	}

}
