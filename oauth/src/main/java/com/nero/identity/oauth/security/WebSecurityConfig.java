package com.nero.identity.oauth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) ->
                requests.requestMatchers("/h2-console/**").permitAll()
                .anyRequest().permitAll()
        )
        .csrf(csrf -> csrf.ignoringRequestMatchers("/client/**")
        		.ignoringRequestMatchers("/user/**")
        		.ignoringRequestMatchers("/h2-console/**")
        		.ignoringRequestMatchers("/token/**")
        )
        .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
		
		return http.build();
	}
	
	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
}
