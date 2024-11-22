package com.nero.identity.oauth.data.repositories.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.repositories.AuthCodeRepository;


@Repository
public class JdbcAuthCodeRepository implements AuthCodeRepository {
	JdbcTemplate jdbc;
	
	@Autowired
	public JdbcAuthCodeRepository(JdbcTemplate jdbc){
		this.jdbc = jdbc;
	}
	
	@Override
	public AuthCode verifyCode(String authorizationCode) {
		try {
			return jdbc.queryForObject(
					"select * from AuthCode where authorizationCode=?",
					this::mapRowToAuthCode,
					authorizationCode
					);
		} catch(EmptyResultDataAccessException ex) {
			return null;
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

	@Override
	public boolean deleteCode(String authorizationCode) {
		// TODO Auto-generated method stub
		String sql = "delete from AuthCode where authorizationCode=?";
		return jdbc.update(sql, authorizationCode) == 1;
	}

}
