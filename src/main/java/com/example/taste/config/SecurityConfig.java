package com.example.taste.config;

import static org.springframework.security.config.Customizer.withDefaults;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.taste.domain.auth.handler.CustomAccessDeniedHandler;
import com.example.taste.domain.auth.handler.CustomAuthenticationEntryPointHandler;
import com.example.taste.domain.auth.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
	private final CustomUserDetailsService userDetailsService;
	private final CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(AbstractHttpConfigurer::disable)
			.cors(withDefaults())        // CORS Config 따름
			.formLogin(AbstractHttpConfigurer::disable)        // 스프링 시큐리티 기본 로그인, 로그아웃 비활성화
			.logout(AbstractHttpConfigurer::disable)
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
			.sessionManagement(sm -> {
					sm.sessionFixation().none();                        // 세션 고정 공격 방어
				}
			)
			.authorizeHttpRequests(auth -> {
				auth.requestMatchers("/auth/**").permitAll();
				auth.requestMatchers("/admin/**").hasRole("ADMIN");
				// ✅ 검색 API 요청 허용
				auth.requestMatchers("/api/search/**").permitAll();
				// ✅ 지도 API 요청 허용
				auth.requestMatchers("/api/map/**").permitAll();
				// 소켓 연결 요청 허용
				auth.requestMatchers("/ws/**", "/ws").permitAll();
				auth.requestMatchers("/h2-console/**").permitAll();
				auth.requestMatchers("/actuator/prometheus").permitAll();
				auth.anyRequest().authenticated();
			})
			.exceptionHandling(exception -> {
				exception.authenticationEntryPoint(customAuthenticationEntryPointHandler) // 인증 예외 핸들러
					.accessDeniedHandler(customAccessDeniedHandler);           // 인가 예외 핸들러
			})
			.userDetailsService(userDetailsService);

		return httpSecurity.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
