package com.nero.identity.oauth.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcClientRepository implements ClientRepository {
	
	private JdbcTemplate jdbc;
	
	@Autowired
	public JdbcClientRepository(JdbcTemplate jdbc) {
		this.jdbc =jdbc;
	}

	@Override
	public Client findClient(String clientId) {
		return jdbc.queryForObject(
				"select clientId, clientSecret, redirectUri from Client where clientId=?", 
				this::mapRowToClient,
				clientId);
	}

	@Override
	public Client saveClient(Client client) {
		jdbc.update(
				"insert into Client(clientId, clientSecret, redirectUri) values(?,?,?)",
				client.getClientId(),
				client.getClientSecret(),
				client.getRedirectUri());
		return client;
	}
	
	private Client mapRowToClient(ResultSet rs, int rowNum)
		throws SQLException {
		Client holder = new Client();
		holder.setClientId(rs.getString("clientId"));
		holder.setClientSecret(rs.getString("clientSecret"));
		holder.setRedirectUri(rs.getString("redirectUri"));
		return holder;
	}

}
