package com.nero.identity.oauth;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.ClientRepository;
import com.nero.identity.oauth.data.User;
import com.nero.identity.oauth.data.UserRepository;

import jakarta.servlet.http.HttpSession;

@RestController
public class AuthorizationController {
	private ClientRepository clientRepo;
	private UserRepository userRepo;
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	public AuthorizationController(ClientRepository clientRepo,
			UserRepository userRepo,
			PasswordEncoder encoder) {
		this.clientRepo = clientRepo;
		this.userRepo = userRepo;
		this.passwordEncoder = encoder;
	}
	
	@PostMapping("/client/register")
	public Client registerClient(@RequestBody Client client){
		int numBytes = 25;
		String secret = generateClientSecret(numBytes);
		client.setClientSecret(secret);
		return this.clientRepo.saveClient(client);
	}

    public static String generateClientSecret(int numBytes) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[numBytes];
        random.nextBytes(bytes);

        // Encode the bytes as a Base64 string for readability
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }
    
    @PostMapping("/user/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
    	userRepo.saveUser(user);
    	return new ResponseEntity<>(user.getUsername(), HttpStatus.OK);
    }
    
    @GetMapping("/user")
    public ResponseEntity<User> getUser(@RequestBody String username){
    	User user = userRepo.findUser(username);
    	return new ResponseEntity<>(user, HttpStatus.OK);
    }
    
    @PostMapping("/user/login")
    public ResponseEntity<String> loginUser(@RequestBody User user) {
    	if(user.getUsername() == null || user.getPassword() == null)
    	{
    		return new ResponseEntity<>("Username and password is required", 
    				HttpStatus.BAD_REQUEST);
    	}
    	User retrievedUser = userRepo.findUser(user.getUsername());
    	if(retrievedUser == null) {
    		return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
    	}
    	
    	String encodedPassword = passwordEncoder.encode(user.getPassword());
    	
    	if(!retrievedUser.getPassword().equals(encodedPassword)) {
    		return new ResponseEntity<>("Access Denied!", HttpStatus.UNAUTHORIZED);
    	}
    	
    	return new ResponseEntity<>(user.getUsername(), HttpStatus.OK);
    }
    
    @GetMapping("/authorization")
    public ResponseEntity<String> authorize(@RequestParam String clientId, 
    		@RequestParam String redirectUri,
    		@RequestParam String authorizationCode,
    		@RequestParam String grantType,
    		HttpSession session) {
    	session.setAttribute("grantType", grantType);
    	session.setAttribute("authorizationCode", authorizationCode);
    	session.setAttribute("redirectUri", redirectUri);
    	session.setAttribute("clientId", clientId);
    	
    	String response = "grantType=" + grantType + ";" 
    			+ "authorizationCode=" + authorizationCode + ";"
    			+ "redirectUri=" + redirectUri + ";" 
    			+ "clientId=" + clientId + ";";
    	
    	return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
