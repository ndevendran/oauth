package com.nero.identity.oauth;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.User;
import com.nero.identity.oauth.data.repositories.AuthCodeRepository;
import com.nero.identity.oauth.data.repositories.ClientRepository;
import com.nero.identity.oauth.data.repositories.UserRepository;
import com.nero.identity.oauth.service.UserService;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthorizationController {
	private ClientRepository clientRepo;
	private UserRepository userRepo;
	private UserService userService;
	private AuthCodeRepository codeRepo;
	
	@Autowired
	public AuthorizationController(ClientRepository clientRepo,
			UserRepository userRepo,
			PasswordEncoder encoder,
			UserService userService,
			AuthCodeRepository codeRepo) {
		this.clientRepo = clientRepo;
		this.userRepo = userRepo;
		this.userService = userService;
		this.codeRepo = codeRepo;
	}
	
	@PostMapping("/client/register")
	@ResponseBody
	public ResponseEntity<Client> registerClient(@RequestBody Client client){
		int numBytes = 25;
		String secret = generateClientSecret(numBytes);
		client.setClientSecret(secret);
		UUID clientId = UUID.randomUUID();
		while(this.clientRepo.findClient(clientId.toString()) != null){
			clientId = UUID.randomUUID();
		}
		
		client.setClientId(clientId);
		return new ResponseEntity<>(this.clientRepo.saveClient(client), HttpStatus.CREATED);
	}

    public static String generateClientSecret(int numBytes) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[numBytes];
        random.nextBytes(bytes);

        // Encode the bytes as a Base64 string for readability
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }
    
    @PostMapping("/user/register")
    @ResponseBody
    public ResponseEntity<User> registerUser(@RequestBody User user) {
    	User savedUser = userService.register(user);
    	
    	if(savedUser == null) {
    		return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    	}
    	
    	return new ResponseEntity<>(savedUser, HttpStatus.OK);
    }
    
    @GetMapping("/user")
    @ResponseBody
    public ResponseEntity<User> getUser(@RequestBody User user){
    	User dbUser = userRepo.findUser(user.getUsername());
    	return new ResponseEntity<>(dbUser, HttpStatus.OK);
    }
    
    @PostMapping("/user/login")
    @ResponseBody
    public ResponseEntity<String> loginUser(@RequestBody User user) {
    	if(!userService.login(user.getUsername(), user.getPassword())) {
    		return new ResponseEntity<>("access_denied", HttpStatus.UNAUTHORIZED);
    	}
    	
    	return new ResponseEntity<>("Login Successful!", HttpStatus.OK);
    }
    
    @GetMapping("/authorization")
    public String authorize(@RequestParam String clientId, 
    		@RequestParam String redirectUri,
    		@RequestParam String state,
    		@RequestParam String responseType,
    		HttpSession session,
    		Model model) {
    	
    	session.setAttribute("responseType", responseType);
    	session.setAttribute("state", state);
    	session.setAttribute("redirectUri", redirectUri);
    	session.setAttribute("clientId", clientId);

    	Client client = this.clientRepo.findClient(clientId);
    	if(client == null) {
    		model.addAttribute("errorMessage", "Invalid Client");
    		return "error";
    	} 
    	
    	if(!client.getRedirectUri().equals(redirectUri)) {
    		String errorMessage = "Invalid Redirect: " + redirectUri;
    		model.addAttribute("errorMessage", errorMessage);
    		return "error";
    	}
    	

    	
    	return "login";
    }
    
    @GetMapping("/approve")
    @ResponseBody
    public ResponseEntity<String> approve(String username, String password, HttpSession session){
		HttpHeaders headers = new HttpHeaders();
		
    	if(!userService.login(username, password)) {
    		String errorMessage = "access_denied. username: " + username + " password: " + password;
    	  	String redirectUrl = UriComponentsBuilder.fromHttpUrl((String) session.getAttribute("redirectUri"))
    		  		.queryParam("error", errorMessage).toUriString();
    			headers.setLocation(URI.create(redirectUrl));
    		return new ResponseEntity<>(headers, HttpStatus.FOUND);
    	}
    	
    	String responseType = (String) session.getAttribute("responseType");
    	
    	if(!responseType.equals("code")) {
    	  	String redirectUrl = UriComponentsBuilder.fromHttpUrl((String) session.getAttribute("redirectUri"))
    		  		.queryParam("error", "unsupported_response_type").toUriString();
    			headers.setLocation(URI.create(redirectUrl));
    		return new ResponseEntity<>(headers, HttpStatus.FOUND);    		
    	}
    	
    	UUID authorizationCode = UUID.randomUUID();
    	
    	while(codeRepo.verifyCode(authorizationCode.toString())) {
    		authorizationCode = UUID.randomUUID();
    	}
    	
    	
    	AuthCode code = new AuthCode();
    	code.setAuthorizationCode(authorizationCode.toString());
    	code.setClientId((String) session.getAttribute("clientId"));
    	
    	String savedCode = codeRepo.saveCode(code);
    	
    	
	  	UriComponentsBuilder redirectUri = UriComponentsBuilder.fromHttpUrl((String) session.getAttribute("redirectUri"))
		  		.queryParam("code", savedCode);
	  	
	  	if(session.getAttribute("state") != null) {
	  		redirectUri.queryParam("state", session.getAttribute("state"));
	  	}
	  	
	  	headers.setLocation(URI.create(redirectUri.toUriString()));
    	
    	return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
    
    @GetMapping("/token")
    @ResponseBody
    public ResponseEntity<String> token(@RequestHeader(value = "Authorization", required = false) String authHeader, 
    		String client_id, String client_secret, String grant_type){
    	String clientId = null;
    	String clientSecret = null;
    	if(authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring(6);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes, StandardCharsets.UTF_8);

            // Split into username and password
            String[] clientDetails = credentials.split(":", 2);
            clientId = clientDetails[0];
            clientSecret = clientDetails.length > 1 ? clientDetails[1] : "";
    	}
    	if(client_id == null && clientId == null) {
    		return new ResponseEntity<>("Invalid Client", HttpStatus.BAD_REQUEST);
    	}
    	
    	if(client_id != null && clientId != null) {
    		return new ResponseEntity<>("Invalid Client", HttpStatus.BAD_REQUEST);
    	}
    	
    	if(clientId == null) {
    		clientId = client_id;
    		clientSecret = client_secret;
    	}
    	
    	Client client = clientRepo.findClient(clientId);
    	if(client == null) {
    		return new ResponseEntity<>("Invalid Client", HttpStatus.BAD_REQUEST);
    	}
    	
    	if(!client.getClientSecret().equals(clientSecret)) {
    		return new ResponseEntity<>("Invalid Client", HttpStatus.BAD_REQUEST);		
    	}
    	
    	if(grant_type != null && grant_type.equals("authorization_code")) {
    		
    	} else {
    		return new ResponseEntity<>("Invalid Client", HttpStatus.BAD_REQUEST);	
    	}
    	
    	
    	return new ResponseEntity<>("Hi", HttpStatus.OK);
    }
}
