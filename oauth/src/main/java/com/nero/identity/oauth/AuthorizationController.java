package com.nero.identity.oauth;

import java.security.SecureRandom;
import java.util.UUID;

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
		UUID clientId = UUID.randomUUID();
		while(this.clientRepo.findClient(clientId.toString()) != null){
			clientId = UUID.randomUUID();
		}
		
		client.setClientId(clientId);
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
    public ResponseEntity<User> registerUser(@RequestBody User user) {
    	if(user.getUsername() == null) {
    		return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    	}
    	
    	if(user.getPassword() == null) {
    		return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    	}
    	User savedUser = userRepo.saveUser(user);
    	return new ResponseEntity<>(savedUser, HttpStatus.OK);
    }
    
    @GetMapping("/user")
    public ResponseEntity<User> getUser(@RequestBody User user){
    	User dbUser = userRepo.findUser(user.getUsername());
    	return new ResponseEntity<>(dbUser, HttpStatus.OK);
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
    	
    	boolean isMatch = passwordEncoder.matches(retrievedUser.getPassword(), user.getPassword());
    	
    	if(!isMatch) {
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
    
    @GetMapping("/approve")
    public ResponseEntity<String> approve(HttpSession session){
    	String response = "";
    	response = response + session.getAttribute("grantType") + ";";
    	response = response + session.getAttribute("authorizationCode") + ";";
    	response = response + session.getAttribute("redirectUri") + ";";
    	response = response + session.getAttribute("clientId") + ";";
    	return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
