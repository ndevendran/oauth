package com.nero.identity.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

@SpringBootTest
public class SmokeTest {
	
	@Autowired
	private AuthorizationController authController;
	
	@Test
	void contextLoads() throws Exception {
		assertThat(authController).isNotNull();
	}
}
