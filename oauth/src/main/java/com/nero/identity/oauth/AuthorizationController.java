package com.nero.identity.oauth;

import java.security.SecureRandom;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.ClientRepository;

@RestController
public class AuthorizationController {
	private ClientRepository clientRepo;
	
	public AuthorizationController(ClientRepository clientRepo) {
		this.clientRepo = clientRepo;
	}
	
	@PostMapping("/client/register")
	@ResponseBody
	public Client registerClient(@RequestBody Client client){
		int numBytes = 25;
		String secret = generateSecretString(numBytes);
		client.setClientSecret(secret);
		return this.clientRepo.saveClient(client);
	}

    public static String generateSecretString(int numBytes) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[numBytes];
        random.nextBytes(bytes);

        // Encode the bytes as a Base64 string for readability
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }
    
    @PostMapping("/user/register")
    @ResponseBody
    public User registerUser(@RequestBody User user) {
    	
    }
}
