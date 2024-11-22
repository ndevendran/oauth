package com.nero.identity.oauth.data.repositories;

import com.nero.identity.oauth.data.User;

public interface UserRepository {
	User findUser(String username);
	User findUser(Long id);
	User saveUser(User user);
}
