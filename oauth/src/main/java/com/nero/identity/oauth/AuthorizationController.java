package com.nero.identity.oauth;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.Token;
import com.nero.identity.oauth.data.User;
import com.nero.identity.oauth.data.repositories.ClientRepository;
import com.nero.identity.oauth.data.repositories.UserRepository;
import com.nero.identity.oauth.service.TokenService;
import com.nero.identity.oauth.service.UserService;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthorizationController {
	private ClientRepository clientRepo;
	private UserRepository userRepo;
	private UserService userService;
	private TokenService tokenService;
	
	@Autowired
	public AuthorizationController(ClientRepository clientRepo,
			UserRepository userRepo,
			UserService userService,
			TokenService tokenService) {
		this.clientRepo = clientRepo;
		this.userRepo = userRepo;
		this.userService = userService;
		this.tokenService = tokenService;
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
		Map<String, String> queryParams = new HashMap<>();
		
    	if(!userService.login(username, password)) {
    		String errorMessage = "access_denied";
    		queryParams.put("error", errorMessage);
    	}
    	
    	String responseType = (String) session.getAttribute("responseType");
    	
    	if(responseType.equals("code")) {
        	String savedCode = tokenService.handleAuthorizationCode((String) session.getAttribute("clientId"));
        	queryParams.put("code", savedCode);
        	Object state = session.getAttribute("state");
        	if(state != null) {
        		queryParams.put("state", (String) state);
        	}
    	} else {
    		queryParams.put("error", "unsupported_response_type");     		
    	}
    	
    	UriComponentsBuilder redirectUrlBuilder = UriComponentsBuilder.fromHttpUrl((String) session.getAttribute("redirectUri"));
    	queryParams.forEach((key,value) -> redirectUrlBuilder.queryParam(key, value));
    	String redirectUrl = redirectUrlBuilder.toUriString();
    	headers.setLocation(URI.create(redirectUrl));
    	return new ResponseEntity<>(headers, HttpStatus.FOUND);    

    }
    
    @GetMapping("/token")
    @ResponseBody
    public ResponseEntity<String> token(@RequestHeader(value = "Authorization", required = false) String authHeader,
    		@RequestBody Map<String, String> requestBody){
    	
    	String client_id = requestBody.get("client_id");
    	String client_secret = requestBody.get("client_secret");
    	String grant_type = requestBody.get("grant_type");
    	String code = requestBody.get("code");
    	ObjectMapper jsonParser = new ObjectMapper();
    	
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
    		return new ResponseEntity<>("invalid_client", HttpStatus.BAD_REQUEST);
    	}
    	
    	if(client_id != null && clientId != null) {
    		return new ResponseEntity<>("invalid_client", HttpStatus.BAD_REQUEST);
    	}
    	
    	if(clientId == null) {
    		clientId = client_id;
    		clientSecret = client_secret;
    	}
    	
    	Client client = clientRepo.findClient(clientId);
    	if(client == null) {
    		return new ResponseEntity<>("invalid_client", HttpStatus.BAD_REQUEST);
    	}
    	
    	if(!client.getClientSecret().equals(clientSecret)) {
    		return new ResponseEntity<>("invalid_client", HttpStatus.BAD_REQUEST);		
    	}
    	if(grant_type == null) {
    		return new ResponseEntity<>("no_grant_type", HttpStatus.BAD_REQUEST);
    	}
    	
    	if(grant_type.equals("authorization_code")) {
    		Token dbToken = this.tokenService.handleAuthorizationCode(code, clientId);
    		if(dbToken != null) {
    			String jsonResponse = null;
    			try {
    				jsonResponse = jsonParser.writeValueAsString(dbToken);
    			} catch(Exception e) {
    				return new ResponseEntity<>(dbToken.getAccessToken().getToken(), HttpStatus.OK);
    			}

    			return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    		} else {
    			return new ResponseEntity<>("invalid_grant", HttpStatus.BAD_REQUEST);
    		}
    	} else if(grant_type.equals("refresh_token")) {
    		//this.tokenService.handleRefreshToken();
    		return new ResponseEntity<>("place_holder", HttpStatus.BAD_REQUEST);
    	} else {
    		return new ResponseEntity<>("unsupported_grant_type: " + grant_type, HttpStatus.BAD_REQUEST);	
    	}
    }
}
