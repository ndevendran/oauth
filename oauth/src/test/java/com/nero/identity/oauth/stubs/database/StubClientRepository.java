package com.nero.identity.oauth.stubs.database;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.repositories.ClientRepository;

public class StubClientRepository implements ClientRepository {
	private Map<Long, Client> database;
	
	public StubClientRepository() {
		this.database = new HashMap<>();
	}

	@Override
	public Client findByClientId(UUID clientId) {
		for(Map.Entry<Long, Client> entry: database.entrySet()) {
			Client dbClient = entry.getValue();
			if(dbClient.getClientId().toString().equals(clientId.toString())) {
				return dbClient;
			}
		}
		
		return null;
	}

	@Override
	public Client save(Client client) {
		database.put(client.getId(), client);
		return client;
	}

	@Override
	public <S extends Client> Iterable<S> saveAll(Iterable<S> entities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Client> findById(Long id) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public boolean existsById(Long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterable<Client> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Client> findAllById(Iterable<Long> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void deleteById(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Client entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAllById(Iterable<? extends Long> ids) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll(Iterable<? extends Client> entities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
		
	}

}
