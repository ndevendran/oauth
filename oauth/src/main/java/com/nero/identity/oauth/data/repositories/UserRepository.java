package com.nero.identity.oauth.data.repositories;

import org.springframework.data.repository.CrudRepository;

import com.nero.identity.oauth.data.User;

public interface UserRepository extends CrudRepository<User, Long> {
//	User findUser(String username);
//	User findUser(Long id);
//	User saveUser(User user);
	User findUserByUsername(String username);
	User findUserById(Long id);
}
