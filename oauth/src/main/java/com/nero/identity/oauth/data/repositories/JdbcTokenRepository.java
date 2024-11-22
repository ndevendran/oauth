package com.nero.identity.oauth.data.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.nero.identity.oauth.data.Token;

@Repository
public class JdbcTokenRepository implements TokenRepository {
	private JdbcTemplate jdbc;
	
	@Autowired
	public JdbcTokenRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}
	
	@Override
	public Token getToken(String token) {
		String sql = "select * from Token where token=?";
		try {
			return jdbc.queryForObject(sql, this::mapRowToToken, token);
		} catch(EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public Boolean deleteToken(String token) {
		String sql = "delete from Token where token=?";
		return jdbc.update(sql, token) == 1;
	}

	@Override
	public Token save(Token token) {
		String sql = "insert into Token(token, clientId, expirationTime) values(?,?, ?)";
		jdbc.update(sql, token.getToken(), token.getClientId(), token.getExpirationTime());
		return token;
	}

	private Token mapRowToToken(ResultSet rs, int rowNum)
			throws SQLException {
			Token token = new Token();
			token.setToken(rs.getString("token"));
			token.setClientId(rs.getString("clientId"));
			token.setExpirationTime(rs.getDate("expirationTime"));
			return token;
		}

}
