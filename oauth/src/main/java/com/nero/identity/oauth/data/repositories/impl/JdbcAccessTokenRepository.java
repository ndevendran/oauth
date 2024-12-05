package com.nero.identity.oauth.data.repositories.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.nero.identity.oauth.data.AccessToken;
import com.nero.identity.oauth.data.Token;
import com.nero.identity.oauth.data.repositories.AccessTokenRepository;
import com.nero.identity.oauth.data.repositories.TokenRepository;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.GeneratedKeyHolder;

@Repository
public class JdbcAccessTokenRepository implements AccessTokenRepository {
	private JdbcTemplate jdbc;
	
	@Autowired
	public JdbcAccessTokenRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}
	
	@Override
	public AccessToken getToken(String token) {
		String sql = "select * from AccessToken where token=?";
		try {
			return jdbc.queryForObject(sql, this::mapRowToToken, token);
		} catch(EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public Boolean deleteToken(Long tokenId) {
		String sql = "delete from AccessToken where id=?";
		return jdbc.update(sql, tokenId) == 1;
	}

	@Override
	public AccessToken save(AccessToken token) {
		KeyHolder keyHolder = new GeneratedKeyHolder();

		String sql = "insert into AccessToken(token, clientId, expirationTime, scope) values(?,?,?,?)";
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, token.getToken());
            ps.setString(2, token.getClientId());
            ps.setDate(3, (Date) token.getExpirationTime());
            ps.setString(4, token.getScope());
            return ps;
        }, keyHolder);
        token.setId(keyHolder.getKey().longValue());
		return token;
	}

	private AccessToken mapRowToToken(ResultSet rs, int rowNum)
			throws SQLException {
			AccessToken token = new AccessToken();
			token.setToken(rs.getString("token"));
			token.setClientId(rs.getString("clientId"));
			token.setExpirationTime(rs.getDate("expirationTime"));
			token.setScope(rs.getString("scope"));
			return token;
		}

}
