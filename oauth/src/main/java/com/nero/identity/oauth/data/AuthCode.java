package com.nero.identity.oauth.data;

import lombok.Data;

@Data
public class AuthCode {
	private String authorizationCode;
	private String clientId;
}
