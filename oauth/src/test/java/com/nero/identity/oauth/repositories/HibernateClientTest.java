package com.nero.identity.oauth.repositories;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.junit.jupiter.api.Test;

import com.nero.identity.oauth.data.Client;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HibernateClientTest {
	
	@Test
	public void storeNewClient() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("oauth");
		try {
			EntityManager em = emf.createEntityManager();
			em.getTransaction().begin();
			Client client = new Client();
			client.setClientId(UUID.randomUUID());
			client.setClientSecret("This is a secret");
			client.setClientName("testClient");
			client.setRedirectUri("www.helloworld.com");
			em.persist(client);
			em.getTransaction().commit();
			List<Client> clients = em.createQuery("SELECT m from Client m", Client.class)
					.getResultList();
			clients.get(0).setClientName("newClient");
			
			//em.getTransaction().commit();
			assertAll(
					()->assertEquals(1, clients.size()),
					()->assertEquals("newClient", clients.get(0).getClientName())
			);
			
			
			em.close();
		} finally {
			emf.close();
		}
	}
	
	@Test
	public void validateClientWithShortClientNameShouldReturnViolation() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Client client = new Client();
		client.setClientId(UUID.randomUUID());
		client.setClientSecret("This is a secret");
		client.setClientName("A");
		client.setRedirectUri("www.helloworld.com");
		Set<ConstraintViolation<Client>> violations = validator.validate(client);
		ConstraintViolation<Client> violation = violations.iterator().next();
		String failedPropertyName = violation.getPropertyPath().iterator().next().getName();
		assertAll(
			()->assertEquals(1, violations.size()),
			()->assertEquals(failedPropertyName, "clientName"),
			()->assertEquals(violation.getMessage(), "Client name is required, minimum 2 characters, maximum 255 characters")
		);
		
	}
	
	@Test
	public void savingClientWithNoClientNameShouldReturnException() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("oauth");
		try {
			EntityManager em = emf.createEntityManager();
			em.getTransaction().begin();
			Client client = new Client();
			client.setClientId(UUID.randomUUID());
			client.setClientSecret("This is a secret");
			client.setRedirectUri("www.helloworld.com");

			assertThrows(ConstraintViolationException.class, () -> em.persist(client));

			em.close();
		} finally {
			emf.close();
		}
	}

}
