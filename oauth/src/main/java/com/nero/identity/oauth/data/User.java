package com.nero.identity.oauth.data;

import lombok.Data;

@Data
public class User {
	private Long id;
	private String username;
	private String password;
}
