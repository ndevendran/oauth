package com.nero.identity.oauth.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
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
					"select * from AuthUser where username=?",
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
					"select * from AuthUser where id=?",
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
		String sql = "insert into AuthUser(username, password) values(?,?)";
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
