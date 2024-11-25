package com.nero.identity.oauth.data;

import lombok.Data;

@Data
public class TokenRequest {
	private String clientId;
	private String clientSecret;
	private String grantType;
	private String code;
	private boolean error;
	private String errorMessage;
}
