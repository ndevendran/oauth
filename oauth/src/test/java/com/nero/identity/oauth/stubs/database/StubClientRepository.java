package com.nero.identity.oauth.stubs.database;

import java.util.HashMap;
import java.util.Map;

import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.repositories.ClientRepository;

public class StubClientRepository implements ClientRepository {
	private Map<Long, Client> database;
	
	public StubClientRepository() {
		this.database = new HashMap<>();
	}

	@Override
	public Client findClient(String clientId) {
		for(Map.Entry<Long, Client> entry: database.entrySet()) {
			Client dbClient = entry.getValue();
			if(dbClient.getClientId().toString().equals(clientId)) {
				return dbClient;
			}
		}
		
		return null;
	}

	@Override
	public Client saveClient(Client client) {
		database.put(client.getId(), client);
		return client;
	}

}
