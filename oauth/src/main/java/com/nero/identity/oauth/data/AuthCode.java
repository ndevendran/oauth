package com.nero.identity.oauth.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class AuthCode {
	@Id
	@GeneratedValue
	private Long id;
	private String authorizationCode;
	private String clientId;
}
