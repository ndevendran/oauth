package com.nero.identity.oauth.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

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
		return jdbc.queryForObject(
				"select * from AuthUser where username=?",
				this::mapRowToUser,
				username
				);
	}

	@Override
	public User findUser(Long id) {
		return jdbc.queryForObject(
				"select * from AuthUser where id=?",
				this::mapRowToUser,
				id
				);
	}

	@Override
	public User saveUser(User user) {
		jdbc.update(
				"insert into AuthUser(username, password) values(?,?)",
				user.getUsername(),
				passwordEncoder.encode(user.getPassword()));
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
