package com.nero.identity.oauth.data;

public interface ClientRepository {
	Client findClient(String clientId);
	Client saveClient(Client client);
}
