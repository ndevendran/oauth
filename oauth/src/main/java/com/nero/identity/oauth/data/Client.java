package com.nero.identity.oauth.data;

import java.util.UUID;

import lombok.Data;

@Data
public class Client {
	private Long id;
	private UUID clientId;
	private String clientSecret;
	private String clientName;
	private String redirectUri;
	private String scope;
}
