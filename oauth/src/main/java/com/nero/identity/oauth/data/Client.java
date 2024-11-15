package com.nero.identity.oauth.data;

import lombok.Data;

@Data
public class Client {
	private String clientId;
	private String clientSecret;
	private String redirectUri;
}
