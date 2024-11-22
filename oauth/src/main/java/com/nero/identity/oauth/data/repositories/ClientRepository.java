package com.nero.identity.oauth.data.repositories;

import com.nero.identity.oauth.data.Client;

public interface ClientRepository {
	Client findClient(String clientId);
	Client saveClient(Client client);
}
