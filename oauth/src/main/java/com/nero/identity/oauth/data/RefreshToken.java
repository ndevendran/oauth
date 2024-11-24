package com.nero.identity.oauth.data;



import java.sql.Date;

import lombok.Data;

@Data
public class RefreshToken {
	private Long id;
	private String token;
	private String clientId;
	private Date expirationTime;
}
