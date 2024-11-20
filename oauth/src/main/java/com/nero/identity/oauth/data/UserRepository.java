package com.nero.identity.oauth.data;

public interface UserRepository {
	User findUser(String username);
	User findUser(Long id);
	User saveUser(User user);
}
