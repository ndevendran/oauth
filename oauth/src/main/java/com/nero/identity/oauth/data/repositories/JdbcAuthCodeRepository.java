package com.nero.identity.oauth.data.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.nero.identity.oauth.data.AuthCode;


@Repository
public class JdbcAuthCodeRepository implements AuthCodeRepository {
	JdbcTemplate jdbc;
	
	@Autowired
	public JdbcAuthCodeRepository(JdbcTemplate jdbc){
		this.jdbc = jdbc;
	}
	
	@Override
	public boolean verifyCode(String authorizationCode) {
		try {
			jdbc.queryForObject(
					"select * from AuthCode where authorizationCode=?",
					this::mapRowToAuthCode,
					authorizationCode
					);
			return true;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		}
	}

	@Override
	public String saveCode(AuthCode code) {
		jdbc.update("insert into AuthCode(authorizationCode, clientId) values(?,?)",
				code.getAuthorizationCode(),
				code.getClientId()
		);
		return code.getAuthorizationCode();
	}

	private AuthCode mapRowToAuthCode(ResultSet rs, int rowNum)
			throws SQLException {
			AuthCode code = new AuthCode();
			code.setAuthorizationCode(rs.getString("authorizationCode"));
			code.setClientId(rs.getString("clientId"));
			return code;
		}

}
