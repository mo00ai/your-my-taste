package com.example.taste.config.security;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
	private final CustomUserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(AbstractHttpConfigurer::disable)
			.cors(AbstractHttpConfigurer::disable)                    // TODO: 실제 배포에선 변경 필요 - @윤예진
			.sessionManagement(sm -> {
					sm.maximumSessions(1).maxSessionsPreventsLogin(true);        // 최대 세션 개수 1, 새 로그인 요청 차단
					sm.sessionFixation().changeSessionId();                        // 세션 고정 공격 방어
				}
			)
			.authorizeHttpRequests(auth -> {
				auth.requestMatchers("/auth/**").permitAll();

				auth.requestMatchers("/ws/**").permitAll();            // MEMO: 웹소켓 테스트용
				auth.requestMatchers("/admin/**").hasRole("ADMIN");
				// ✅ 검색 API 요청 허용
				auth.requestMatchers("/api/search/**").permitAll();
				// ✅ 지도 API 요청 허용
				auth.requestMatchers("/api/map/**").permitAll();
				// 소켓 연결 요청 허용
				auth.requestMatchers("/ws/**", "/ws").permitAll();
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
