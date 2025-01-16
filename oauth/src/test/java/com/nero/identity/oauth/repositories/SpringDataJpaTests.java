package com.nero.identity.oauth.repositories;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import com.nero.identity.oauth.data.AuthCode;
import com.nero.identity.oauth.data.repositories.AuthCodeRepository;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpringDataJpaTests {
	@Autowired
	AuthCodeRepository authCodeRepo;
	
	@BeforeAll
	void beforeAll() {
		List<AuthCode> codes = new ArrayList<>();
		for(int i = 0; i<10; i++) {
			String testCode = "testCode" + i;
			String testClient = "testClient" + i;
			AuthCode authCode = new AuthCode();
			authCode.setAuthorizationCode(testCode);
			authCode.setClientId(testClient);
			codes.add(authCode);
		}
		
		authCodeRepo.saveAll(codes);

	}
	
	@Test
	void testSave() {
		AuthCode authCode = new AuthCode();
		authCode.setAuthorizationCode("testCode11");
		authCode.setClientId("testClient");
		authCode = authCodeRepo.save(authCode);
		assertNotNull(authCode);
	}
	
	@Test
	void testFindAll() {
		List<AuthCode> codes = authCodeRepo.findAll();
		assertEquals(10, codes.size());
	}
	
	@Test
	void testFindByAuthorizationCode() {
		List<AuthCode> codes = authCodeRepo.findByAuthorizationCode("testCode1");
		assertNotNull(codes.get(0));
		assertEquals("testCode1", codes.get(0).getAuthorizationCode());
	}
	
	@Test
	void testFindAllByOrderByAuthorizationCodeAsc() {
		List<AuthCode> codes = authCodeRepo.findAllByOrderByAuthorizationCode();
		assertNotNull(codes);
		assertEquals(codes.get(0).getAuthorizationCode(), "testCode0");
	}
	
	@Test
	void testFindByAuthorizationCodeLike() {
		List<AuthCode> codes = authCodeRepo.findByAuthorizationCodeLike("testCode11");
		assertNotNull(codes);
		assertEquals(codes.size(), 1);
	}
	
	@Test
	void testFindAllWithPaging() {
		List<AuthCode> codes = authCodeRepo.findAllByOrderByAuthorizationCode(PageRequest.of(0, 3));
		assertEquals(3, codes.size());
		assertEquals(codes.get(0).getAuthorizationCode(), "testCode0");
		List<AuthCode> nextPage = authCodeRepo.findAllByOrderByAuthorizationCode(PageRequest.of(1, 3));
		assertEquals(nextPage.get(0).getAuthorizationCode(), "testCode3");
	}
}
