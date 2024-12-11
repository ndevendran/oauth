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

import com.nero.identity.oauth.data.Client;
import com.nero.identity.oauth.data.User;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthorizationControllerTest {
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
	private static int state = 5;
	
	
	@BeforeAll
	static void setUp() {
		clientName = "stuff";
		redirectUri = "http://ww.fake.com/redirect";
		username = "testUser";
		password = "test";
	}
	
	@Test
	@Order(1)
	void registerClientShouldReturnNewlyCreatedClient() throws Exception {
		Map<String, String> requestMap = new HashMap<>();
		requestMap.put("clientName", clientName);
		requestMap.put("redirectUri", redirectUri);
		
		ResponseEntity<Client> response = this.restTemplate.postForEntity("http://localhost:" + port + "/client/register", requestMap, Client.class);
		Client registeredClient = response.getBody();
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertThat(registeredClient.getClientName()).isEqualTo(clientName);
		assertThat(registeredClient.getRedirectUri()).isEqualTo(redirectUri);
		assertNotNull(registeredClient.getClientId(), "Client ID should not be null");
		assertNotNull(registeredClient.getClientSecret(), "Client Secret should not be null");
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
	void getAuthorizationEndpointShouldReturnLoginPage() throws Exception {
		String url = "http://localhost:"+port+"/authorization";
		String uriWithParams = UriComponentsBuilder.fromUriString(url)
				.queryParam("clientId", clientId)
				.queryParam("redirectUri", redirectUri)
				.queryParam("state", state)
				.queryParam("responseType", "code")
				.toUriString();
		
		ResponseEntity<String> response = this.restTemplate.getForEntity(uriWithParams, String.class);
		
		assertEquals(HttpStatus.OK, response.getStatusCode());	
		Document doc = Jsoup.parse(response.getBody());
		String title = doc.title();
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
	@Order(4)
	void getApproveEndpointShouldReturnRedirect() throws Exception {
		String url = "http://localhost:"+port+"/approve";
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		assertNotNull(loginCsrf);
		formData.add("username", username);
		formData.add("password", password);

		
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
		
		assertNotNull(queryParams.get("code"));
		assertNotNull(queryParams.get("state"));
		assertEquals(queryParams.get("state"), Integer.toString(state));
	}
}
