package com.nero.identity.oauth.controller;


import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import com.nero.identity.oauth.AuthorizationController;
import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.User;
import com.nero.identity.oauth.data.repositories.ClientRepository;
import com.nero.identity.oauth.data.repositories.UserRepository;
import com.nero.identity.oauth.service.TokenService;
import com.nero.identity.oauth.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthorizationRestControllerTest {
	private static final Logger log = LoggerFactory.getLogger(AuthorizationRestControllerTest.class);
	
	@Autowired
	MockMvc mvc;
	
	@Mock
	private ClientRepository clientRepository;
	
	@Mock
	private UserService userService;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private TokenService tokenService;
	
	@InjectMocks
	private AuthorizationController authorizationController;
	
	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.standaloneSetup(authorizationController).build();
	}
	
	
	@Test
	void testRegisterClient() throws Exception {
		Client client = new Client();
		client.setClientId(UUID.randomUUID());
		client.setClientName("stuff");
		client.setClientSecret("Secret");
		client.setId(1L);
		client.setRedirectUri("www.fake.com");
		when(clientRepository.findByClientId(any(UUID.class))).thenReturn(null);
		when(clientRepository.save(any())).thenReturn(client);
		
        String clientJson = """
                {
				    "clientName": "stuff",
				    "redirectUri": "http://www.fake.com/redirect"
                }
                """;
        
		mvc.perform(post("/client/register").contentType(MediaType.APPLICATION_JSON).content(clientJson))
		.andExpect(status().isCreated())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
		.andExpect(jsonPath("$").exists())
		.andExpect(jsonPath("$.clientName").value("stuff"))
		.andExpect(jsonPath("$.redirectUri").value("www.fake.com"));
	}
	
	@Test
	void testRegisterUser() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setPassword("test");
		user.setUsername("testUser");
		when(userService.register(any(User.class))).thenReturn(user);
		
		String userJson =
			"""
			{
			    "username": "testUser",
			    "password": "test"
			}
			""";
		
		mvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content(userJson))
			.andExpect(status().isCreated())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").exists())
			.andExpect(jsonPath("$.username").value("testUser"))
			.andExpect(jsonPath("$.password").exists())
			.andExpect(jsonPath("$.id").exists());
		
	}
	
	@Test
	void testGetUser() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setPassword("test");
		user.setUsername("testUser");
		when(userRepository.findUserByUsername("testUser")).thenReturn(user);
		
		String userJson =
				"""
				{
				    "username": "testUser"
				}
				""";
		
		mvc.perform(get("/user").contentType(MediaType.APPLICATION_JSON).content(userJson))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").exists())
			.andExpect(jsonPath("$.username").value("testUser"));
	}
	
	@Test
	void testLoginUser() throws Exception {
		when(userService.login("testUser", "test")).thenReturn(true);
		
		String userJson = 
				"""
					{
						"username": "testUser",
						"password": "test"
					}
				""";
		
		mvc.perform(post("/user/login").contentType(MediaType.APPLICATION_JSON).content(userJson))
			.andExpect(status().isOk());
	}
	
	@Test
	void testAuthorizationEndpoint() throws Exception {
		Client client = new Client();
		client.setClientId(UUID.randomUUID());
		client.setClientName("stuff");
		client.setClientSecret("Secret");
		client.setId(1L);
		client.setRedirectUri("www.fake.com");
		when(clientRepository.findByClientId(client.getClientId())).thenReturn(client);
		
		mvc.perform(get("/authorization")
				.param("clientId", client.getClientId().toString())
				.param("redirectUri", client.getRedirectUri())
				.param("state", "5")
				.param("responseType", "code"))
				.andExpect(status().isOk());
	}
	
	@Test
	void testApproveEndpoint() throws Exception {
		when(userService.login("testUser", "test")).thenReturn(true);
		
		AuthCode code = new AuthCode();
		code.setAuthorizationCode("test_code");
		when(tokenService.generateAuthorizationCode(any())).thenReturn(code);
		
		mvc.perform(post("/approve")
				.param("username", "testUser")
				.param("password", "test")
				.sessionAttr("responseType", "code")
				.sessionAttr("redirectUri", "http://www.fake.com"))
		.andExpect(status().isFound())
		.andExpect(header().string("Location", "http://www.fake.com?code=test_code"));

	}
	
	@Test
	void testTokenEndpoint() throws Exception {
		
	}
	
	
}
