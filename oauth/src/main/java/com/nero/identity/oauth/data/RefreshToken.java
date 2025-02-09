package com.nero.identity.oauth.data;



import java.sql.Date;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Embeddable
public class RefreshToken {
	@NotNull
	private String token;
	
	@NotNull
	private Date expirationTime;
}
