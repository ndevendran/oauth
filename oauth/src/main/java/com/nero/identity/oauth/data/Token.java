package com.nero.identity.oauth.data;

import java.util.Date;

import lombok.Data;

@Data
public class Token {
	private String token;
	private String clientId;
	private Date expirationTime;
}
