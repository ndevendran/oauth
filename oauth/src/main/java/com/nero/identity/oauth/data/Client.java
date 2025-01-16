package com.nero.identity.oauth.data;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
public class Client {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	private UUID clientId;
	
	@NotNull
	private String clientSecret;
	
	@NotNull
	@Size(
			min = 2,
			max = 255,
			message = "Client name is required, minimum 2 characters, maximum 255 characters"
	)
	private String clientName;
	private String redirectUri;
	private String scope;
}
