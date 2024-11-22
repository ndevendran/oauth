package com.nero.identity.oauth.data.repositories.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.repositories.ClientRepository;

import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.GeneratedKeyHolder;

@Repository
public class JdbcClientRepository implements ClientRepository {
	
	private JdbcTemplate jdbc;
	
	@Autowired
	public JdbcClientRepository(JdbcTemplate jdbc) {
		this.jdbc =jdbc;
	}

	@Override
	public Client findClient(String clientId) {
		try {
			return jdbc.queryForObject(
					"select clientId, clientSecret, clientName, redirectUri from Client where clientId=?", 
					this::mapRowToClient,
					clientId);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}

	}

	@Override
	public Client saveClient(Client client) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		String sql = "insert into Client(clientId, clientSecret, redirectUri, clientName) values(?,?,?,?)";
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, client.getClientId().toString());
            ps.setString(2, client.getClientSecret());
            ps.setString(3, client.getRedirectUri());
            ps.setString(4, client.getClientName());
            return ps;
        }, keyHolder);
        client.setId(keyHolder.getKey().longValue());
		return client;
	}
	
	private Client mapRowToClient(ResultSet rs, int rowNum)
		throws SQLException {
		Client holder = new Client();
		holder.setClientId(UUID.fromString(rs.getString("clientId")));
		holder.setClientSecret(rs.getString("clientSecret"));
		holder.setRedirectUri(rs.getString("redirectUri"));
		holder.setClientName(rs.getString("clientName"));
		return holder;
	}

}
