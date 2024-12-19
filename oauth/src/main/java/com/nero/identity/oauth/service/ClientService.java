package com.nero.identity.oauth.service;

import java.util.UUID;

import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.repositories.AuthCodeRepository;
import com.nero.identity.oauth.data.repositories.ClientRepository;

public class ClientService {
	private ClientRepository clientRepo;
	private AuthCodeRepository authCodeRepo;
	
	public ClientService(ClientRepository clientRepo, AuthCodeRepository authCodeRepo) {
		this.clientRepo = clientRepo;
		this.authCodeRepo = authCodeRepo;
	}
	
	public Client registerClient(Client client) {
		return clientRepo.saveClient(client);
	}
	
	public AuthCode generateAuthorizationCode(String clientId) {
		UUID authorizationCode = UUID.randomUUID();
    	
    	while(authCodeRepo.verifyCode(authorizationCode.toString()) != null) {
    		authorizationCode = UUID.randomUUID();
    	}
    	
    	
    	AuthCode code = new AuthCode();
    	code.setAuthorizationCode(authorizationCode.toString());
    	code.setClientId(clientId);
    	
    	return authCodeRepo.saveCode(code);
	}
}
