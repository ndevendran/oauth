package com.nero.identity.oauth.data.repositories.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.nero.identity.oauth.data.Token;
import com.nero.identity.oauth.data.repositories.TokenRepository;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.GeneratedKeyHolder;

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
		KeyHolder keyHolder = new GeneratedKeyHolder();

		String sql = "insert into Token(token, clientId, expirationTime) values(?,?,?)";
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, token.getToken());
            ps.setString(2, token.getClientId());
            ps.setDate(3, (Date) token.getExpirationTime());
            return ps;
        }, keyHolder);
        token.setId(keyHolder.getKey().longValue());
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
