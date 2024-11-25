package com.nero.identity.oauth.data.repositories;

import com.nero.identity.oauth.data.RefreshToken;

public interface RefreshTokenRepository {
	RefreshToken getRefreshToken(String refreshToken);
	RefreshToken saveRefreshToken(RefreshToken refreshToken);
	Long getAccessTokenId(Long refreshTokenId);
	Long updateRefreshTokenWithNewAccessToken(Long refreshTokenId, Long accessTokenId);
}
