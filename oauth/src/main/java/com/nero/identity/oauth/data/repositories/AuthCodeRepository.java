package com.nero.identity.oauth.data.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.nero.identity.oauth.data.AuthCode;

public interface AuthCodeRepository extends CrudRepository<AuthCode, Long> {
//	AuthCode verifyCode(String authorizationCode);
//	boolean deleteCode(String authorizationCode);
//	AuthCode saveCode(AuthCode authCode);
	List<AuthCode> findAll();
	AuthCode findByAuthorizationCode(String authorizationCode);
	List<AuthCode> findAllByOrderByAuthorizationCode();
	List<AuthCode> findByAuthorizationCodeLike(String authorizationCode);
	List<AuthCode> findAll(Pageable pageable);
	List<AuthCode> findAllByOrderByAuthorizationCode(Pageable pageable);
	void deleteByAuthorizationCode(String authorizationCode);
}
