package com.example.taste.config.security;

import static org.springframework.security.config.Customizer.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
	private final CustomUserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity
			.cors(withDefaults())        // CORS Config 따름
			.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/auth/**", "/web-push/subscribe"))
			//.csrf(csrf -> csrf.disable()) //테스트용 disable
			.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
			.sessionManagement(sm -> {
					sm.maximumSessions(1).maxSessionsPreventsLogin(true);        // 최대 세션 개수 1, 새 로그인 요청 차단
					sm.sessionFixation().changeSessionId();                        // 세션 고정 공격 방어
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
				// 프로메테우스 허용
				auth.requestMatchers("/actuator/prometheus").permitAll();
				// 엔드투엔드 테스트용 index 접근 허용
				auth.requestMatchers("/index.html", "/sw.js").permitAll();
				auth.anyRequest().authenticated();
			})
			.userDetailsService(userDetailsService);

		return httpSecurity.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
