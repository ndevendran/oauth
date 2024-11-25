package com.nero.identity.oauth.data.repositories.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.nero.identity.oauth.data.RefreshToken;
import com.nero.identity.oauth.data.repositories.RefreshTokenRepository;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.support.GeneratedKeyHolder;

@Repository
public class JdbcRefreshTokenRepository implements RefreshTokenRepository {
	private JdbcTemplate jdbc;
	
	@Autowired
	public JdbcRefreshTokenRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public RefreshToken getRefreshToken(String refreshToken) {
		String sql = "select * from RefreshToken where refreshToken=?";
		try {
			return jdbc.queryForObject(sql, this::mapRowToRefreshToken, refreshToken);
		} catch(EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public RefreshToken saveRefreshToken(RefreshToken refreshToken) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		String sql = "insert into RefreshToken(refreshToken, clientId, expirationTime) values(?,?,?)";
		String bindSql = "insert into RefreshToken_AccessToken(refreshToken, token) values(?,?)";
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, refreshToken.getToken());
            ps.setString(2, refreshToken.getClientId());
            ps.setDate(3, (Date) refreshToken.getExpirationTime());
            return ps;
        }, keyHolder);
        refreshToken.setId(keyHolder.getKey().longValue());
        
		return refreshToken;
	}

	@Override
	public Long updateRefreshTokenWithNewAccessToken(Long refreshTokenId, Long accessTokenId) {
		String deleteSql = "delete from RefreshToken_AccessToken where refreshToken=?";
		jdbc.update(deleteSql, refreshTokenId);
		String bindSql = "insert into RefreshToken_AccessToken(refreshToken, token) values(?,?)";
		jdbc.update(bindSql, refreshTokenId, accessTokenId);
		return refreshTokenId;
	}
	
	private RefreshToken mapRowToRefreshToken(ResultSet rs, int rowNum)
			throws SQLException {
			RefreshToken refreshToken = new RefreshToken();
			refreshToken.setId(rs.getLong("id"));
			refreshToken.setToken(rs.getString("refreshToken"));
			refreshToken.setClientId(rs.getString("clientId"));
			refreshToken.setExpirationTime(rs.getDate("expirationTime"));
			return refreshToken;
		}
	
	private Long mapRowToLong(ResultSet rs, int rowNum)
		throws SQLException {
		Long token = rs.getLong("token");
		return token;
	}

	@Override
	public Long getAccessTokenId(Long refreshTokenId) {
		String sql = "select * from RefreshToken_AccessToken where refreshToken=?";
		try {
			return jdbc.queryForObject(sql, this::mapRowToLong, refreshTokenId);
		} catch(EmptyResultDataAccessException ex) {
			return null;
		}
	}

}
