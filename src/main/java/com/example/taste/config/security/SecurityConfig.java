package com.example.taste.config.security;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(sm
				-> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))    // TODO: 세션 매니저
			.authorizeHttpRequests(auth
				-> {
				auth.requestMatchers("/auth/**").permitAll();

				// TODO: 인증 경로 수정
				// "/stores/*/waiting" USER도 허용
				auth.requestMatchers("/stores/*/waiting").hasRole("USER");
				// "/store/*/reservation" USER도 허용
				auth.requestMatchers("/stores/*/reservation").hasRole("USER");
				auth.anyRequest().authenticated();
			});

		return httpSecurity.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
