package com.nero.identity.oauth.data.repositories;

import org.springframework.data.repository.CrudRepository;

import com.nero.identity.oauth.data.AccessToken;

public interface AccessTokenRepository extends CrudRepository<AccessToken, Long> {
	AccessToken findByToken(String token);
	AccessToken findByRefreshTokenToken(String refreshToken);
}
