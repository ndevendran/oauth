package com.nero.identity.oauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nero.identity.oauth.data.User;
import com.nero.identity.oauth.data.repositories.UserRepository;

@Service
public class UserService {
	private UserRepository userRepo;
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
	}
	
	public boolean login(String username, String password) {
		User user = userRepo.findUserByUsername(username);
		if(user == null) {
			return false;
		}

		if(passwordEncoder.matches(password, user.getPassword())) {
			return true;
		} else {
			return false;
		}
		
	}
	
	public User register(User user) {
    	if(user.getUsername() == null) {
    		return null;
    	}
    	
    	if(userRepo.findUserByUsername(user.getUsername()) != null) {
    		return null;
    	}
    	
    	if(user.getPassword() == null) {
    		return null;
    	}

		user.setPassword(passwordEncoder.encode(user.getPassword()));
    	User savedUser = userRepo.save(user);
    	return savedUser;
		
	}
}
