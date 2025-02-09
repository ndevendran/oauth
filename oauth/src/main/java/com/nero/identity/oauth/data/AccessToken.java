package com.nero.identity.oauth.data;

import java.sql.Date;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class AccessToken {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	private String token;
	
	@NotNull
	private String clientId;
	
	@NotNull
	private Date expirationTime;
	
	private String scope;
	
	@AttributeOverride(
			name="token",
			column=@Column(name="REFRESH_TOKEN")
	)
	@AttributeOverride(
			name="expirationTime",
			column=@Column(name="REFRESH_TOKEN_EXPIRATION_TIME")
	)
	private RefreshToken refreshToken;
}
