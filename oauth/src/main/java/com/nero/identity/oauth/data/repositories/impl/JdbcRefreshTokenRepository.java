package com.nero.identity.oauth.data.repositories.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.nero.identity.oauth.data.RefreshToken;
import com.nero.identity.oauth.data.repositories.RefreshTokenRepository;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.GeneratedKeyHolder;

public class JdbcRefreshTokenRepository implements RefreshTokenRepository {
	private JdbcTemplate jdbc;
	
	@Autowired
	public JdbcRefreshTokenRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public RefreshToken getRefreshToken(String refreshToken) {
		String sql = "select * from RefreshToken where refreshToken=?";
		jdbc.queryForObject(sql, this::mapRowToRefreshToken, refreshToken);
		return null;
	}

	@Override
	public RefreshToken saveRefreshToken(RefreshToken refreshToken, String accessTokenId) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		String sql = "insert into RefreshToken(refreshToken, clientId, expirationTime) values(?,?,?)";
		String bindSql = "insert into RefreshToken_Token(refreshToken, token) values(?,?)";
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, refreshToken.getRefreshToken());
            ps.setString(2, refreshToken.getClientId());
            ps.setDate(3, (Date) refreshToken.getExpirationTime());
            return ps;
        }, keyHolder);
        refreshToken.setId(keyHolder.getKey().longValue());
        
        jdbc.update(bindSql, refreshToken.getId(), accessTokenId);
		return refreshToken;
	}

	@Override
	public RefreshToken updateRefreshTokenWithNewAccessToken(RefreshToken refreshToken, Long accessTokenId) {
		String deleteSql = "delete from RefreshToken_Token where refreshToken=?";
		jdbc.update(deleteSql, refreshToken.getId());
		String bindSql = "insert into RefreshToken_Token(refreshToken, token) values(?,?)";
		jdbc.update(bindSql, refreshToken.getId(), accessTokenId);
		return refreshToken;
	}
	
	private RefreshToken mapRowToRefreshToken(ResultSet rs, int rowNum)
			throws SQLException {
			RefreshToken refreshToken = new RefreshToken();
			refreshToken.setId(rs.getLong("id"));
			refreshToken.setRefreshToken(rs.getString("refreshToken"));
			refreshToken.setClientId(rs.getString("clientId"));
			refreshToken.setExpirationTime(rs.getDate("expirationTime"));
			return refreshToken;
		}

}
