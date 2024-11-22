package com.nero.identity.oauth.data;

import java.util.Date;

import lombok.Data;

@Data
public class RefreshToken {
	private Long id;
	private Long tokenId;
	private String refreshToken;
	private String clientId;
	private Date expirationTime;
}
