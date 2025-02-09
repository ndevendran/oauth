package com.nero.identity.oauth.data.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.nero.identity.oauth.data.Client;

public interface ClientRepository extends CrudRepository<Client, Long> {
//	Client findClient(String clientId);
//	Client saveClient(Client client);
	Client findByClientId(UUID clientId);
	
}
