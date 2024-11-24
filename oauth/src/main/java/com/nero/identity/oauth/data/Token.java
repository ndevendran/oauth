package com.nero.identity.oauth.data;

import java.util.Date;

import lombok.Data;

@Data
public class Token {
	private AccessToken accessToken;
	private RefreshToken refreshToken;
}
