package com.nero.identity.oauth.data.repositories.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.nero.identity.oauth.data.User;
import com.nero.identity.oauth.data.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.GeneratedKeyHolder;

@Repository
public class JdbcUserRepository implements UserRepository {
	private JdbcTemplate jdbc;
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	public JdbcUserRepository(JdbcTemplate jdbc, PasswordEncoder encoder) {
		this.jdbc = jdbc;
		this.passwordEncoder = encoder;
	}
	
	@Override
	public User findUser(String username) {
		try {
			return jdbc.queryForObject(
					"select * from \"User\" where username=?",
					this::mapRowToUser,
					username
					);
		} catch(EmptyResultDataAccessException ex) {
			return null;
		}

	}

	@Override
	public User findUser(Long id) {
		try {
			return jdbc.queryForObject(
					"select * from \"User\" where id=?",
					this::mapRowToUser,
					id
					);			
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public User saveUser(User user) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		String sql = "insert into \"User\"(username, password) values(?,?)";
		jdbc.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
			ps.setString(1, user.getUsername());
			ps.setString(2, user.getPassword());
			return ps;
		}, keyHolder);
		user.setId(keyHolder.getKey().longValue());
		return user;
	}
	
	private User mapRowToUser(ResultSet rs, int rowNum)
			throws SQLException {
			User holder = new User();
			holder.setUsername(rs.getString("username"));
			holder.setPassword(rs.getString("password"));
			holder.setId(rs.getLong("id"));
			return holder;
		}

}
