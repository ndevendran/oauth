package com.nero.identity.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.Token;
import com.nero.identity.oauth.data.User;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthWithScopeTest {
	@LocalServerPort
	private int port;
	
	@Autowired
	private TestRestTemplate restTemplate;
	
	private static String clientName;
	private static String redirectUri;
	private static String username;
	private static String password;
	private static String clientId;
	private static String clientSecret;
	private static String loginCsrf;
	private static String cookie;
	private static String authorizationCode;
	private static String refreshToken;
	private static int state = 5;
	
	@BeforeAll
	static void setUp() {
		clientName = "stuff";
		redirectUri = "http://www.fake.com/redirect";
		username = "testUser";
		password = "test";
	}
	
	@Test
	@Order(1)
	void registerClientWithScopeShouldReturnNewlyCreatedClient() throws Exception {
		Map<String, String> requestMap = new HashMap<>();
		String scope = "read write delete";
		requestMap.put("clientName", clientName);
		requestMap.put("redirectUri", redirectUri);
		requestMap.put("scope", scope);
		
		ResponseEntity<Client> response = this.restTemplate.postForEntity("http://localhost:" + port + "/client/register", requestMap, Client.class);
		Client registeredClient = response.getBody();
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertThat(registeredClient.getClientName()).isEqualTo(clientName);
		assertThat(registeredClient.getRedirectUri()).isEqualTo(redirectUri);
		assertNotNull(registeredClient.getClientId(), "Client ID should not be null");
		assertNotNull(registeredClient.getClientSecret(), "Client Secret should not be null");
		assertNotNull(registeredClient.getScope());
		assertEquals(registeredClient.getScope(), scope);
		clientId = registeredClient.getClientId().toString();
		clientSecret = registeredClient.getClientSecret();
	}
	
	@Test
	@Order(2)
	void registerUserShouldReturnNewlyCreatedUser() throws Exception {
		Map<String, String> requestMap = new HashMap<>();
		requestMap.put("username", username);
		requestMap.put("password", password);
		ResponseEntity<User> response = this.restTemplate.postForEntity("http://localhost:"+port+"/user/register", requestMap, User.class);
		User registeredUser = response.getBody();
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(registeredUser.getUsername(), username);
	}
	
	@Test
	@Order(3)
	void getAuthorizationEndpointWithBadScopeShouldReturnErrorPage() throws Exception {
		String url = "http://localhost:"+port+"/authorization";
		String uriWithParams = UriComponentsBuilder.fromUriString(url)
				.queryParam("clientId", clientId)
				.queryParam("redirectUri", redirectUri)
				.queryParam("state", state)
				.queryParam("responseType", "code")
				.queryParam("scope", "read write clean")
				.build()
				.toUriString();
		
		ResponseEntity<String> response = this.restTemplate.getForEntity(uriWithParams, String.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());	
		Document doc = Jsoup.parse(response.getBody());
		String title = doc.title();
		assertNotNull(title);
		assertEquals("Error", title);
		
		Element header = doc.selectFirst("h1");
		assertNotNull(header);
		assertEquals(header.text(), "Invalid Scope");
	}

	@Test
	@Order(4)
	void getAuthorizationEndpointWithGoodScopeShouldReturnLoginPage() throws Exception {
		String url = "http://localhost:"+port+"/authorization";
		String uriWithParams = UriComponentsBuilder.fromUriString(url)
				.queryParam("clientId", clientId)
				.queryParam("redirectUri", redirectUri)
				.queryParam("state", state)
				.queryParam("responseType", "code")
				.queryParam("scope", "read write")
				.build()
				.toUriString();
		
		ResponseEntity<String> response = this.restTemplate.getForEntity(uriWithParams, String.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());	
		Document doc = Jsoup.parse(response.getBody());
		String title = doc.title();
		System.out.println(doc.selectFirst("h1").text());
		assertEquals("Login", title);
		

		
		Element form = doc.selectFirst("form");
		assertEquals(form.attr("action"), "/approve");
		assertEquals(form.attr("method"), "post");
		
		Elements inputs = doc.select("input");
		for(Element input : inputs ) {
			String name = input.attr("name");
			if(name.equals("username")) {
				assertEquals(input.attr("type"), "text");
			} 
			
			if(name.equals("password")){
				assertEquals(input.attr("type"), "password");
			} 
			
			if(name.equals("_csrf")) {
				assertEquals(input.attr("type"), "hidden");
				loginCsrf = input.attr("value");
				assertNotNull(loginCsrf);
			}
		}
		
		cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
	}
	
	@Test
	@Order(5)
	void getApproveEndpointShouldReturnRedirect() throws Exception {
		String url = "http://localhost:"+port+"/approve";
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		assertNotNull(loginCsrf);
		formData.add("username", username);
		formData.add("password", password);
		formData.add("scope", "read");
		formData.add("scope", "write");

		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.add(HttpHeaders.COOKIE, cookie);
		headers.add("X-CSRF-TOKEN", loginCsrf);
		
		HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<>(formData, headers);
		ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
		String queryString = response.getHeaders().getLocation().getQuery();
		Map<String, String> queryParams = new HashMap<>();
		String[] params = queryString.split("&");
		for(String param : params ) {
			String[] keyValue = param.split("=", 2);
			String key = keyValue[0];
			String value = keyValue[1];
			queryParams.put(key, value);
		}
		
		
		authorizationCode = queryParams.get("code");
		
		assertNotNull(authorizationCode);
		assertNotNull(queryParams.get("state"));
		assertEquals(queryParams.get("state"), Integer.toString(state));
		cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
	}

	@Test
	@Order(6)
	void getTokenEndpointShouldReturnToken() throws Exception {
		String url = "http://localhost:"+port+"/token";
		Map<String, String> requestMap = new HashMap<>();
		requestMap.put("grant_type", "authorization_code");
		requestMap.put("code", authorizationCode);
		requestMap.put("client_id", clientId);
		requestMap.put("client_secret", clientSecret);
		
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonBody = objectMapper.writeValueAsString(requestMap);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.COOKIE, cookie);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Token responseToken = objectMapper.readValue(response.getBody(), Token.class);
		assertNotNull(responseToken);
		assertNotNull(responseToken.getAccessToken());
		assertNotNull(responseToken.getRefreshToken());
		assertNotNull(responseToken.getAccessToken().getToken());
		assertNotNull(responseToken.getRefreshToken().getToken());
		refreshToken = responseToken.getRefreshToken().getToken();
	}
}
