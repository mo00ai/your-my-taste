package com.example.taste.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.taste.domain.auth.filter.UserAuthenticationFilter;
import com.example.taste.domain.auth.handler.CustomAccessDeniedHandler;
import com.example.taste.domain.auth.handler.CustomAuthenticationEntryPointHandler;
import com.example.taste.domain.auth.handler.CustomLogoutHandler;
import com.example.taste.domain.auth.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
	private final ObjectMapper objectMapper;
	private final ObjectPostProcessor<Object> objectPostProcessor;
	private final CustomUserDetailsService userDetailsService;
	private final CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;
	private final CustomLogoutHandler customLogoutHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(objectPostProcessor);
		builder.userDetailsService(userDetailsService)
			.passwordEncoder(passwordEncoder());
		AuthenticationManager authManager = builder.build();
		UserAuthenticationFilter userAuthenticationFilter = new UserAuthenticationFilter(objectMapper);
		userAuthenticationFilter.setFilterProcessesUrl("/auth/signin");
		httpSecurity.csrf(AbstractHttpConfigurer::disable)
			.cors(AbstractHttpConfigurer::disable)                    // TODO: 실제 배포에선 변경 필요 - @윤예진
			// .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
			.sessionManagement(sm -> {
					sm.maximumSessions(1).maxSessionsPreventsLogin(true);        // 최대 세션 개수 1, 새 로그인 요청 차단
					sm.sessionFixation().changeSessionId();                        // 세션 고정 공격 방어
				}
			)
			.authorizeHttpRequests(auth -> {
				auth.requestMatchers("/auth/signup").permitAll();
				auth.requestMatchers("/ws/**").permitAll();            // MEMO: 웹소켓 테스트용
				auth.requestMatchers("/admin/**").hasRole("ADMIN");
				// ✅ 검색 API 요청 허용
				auth.requestMatchers("/api/search/**").permitAll();
				// ✅ 지도 API 요청 허용
				auth.requestMatchers("/api/map/**").permitAll();
				// 소켓 연결 요청 허용
				auth.requestMatchers("/ws/**", "/ws").permitAll();
				auth.requestMatchers("/h2-console/**").permitAll();
				auth.anyRequest().authenticated();
			})
			.exceptionHandling((exceptionConfig) ->
				exceptionConfig.authenticationEntryPoint(customAuthenticationEntryPointHandler)
					.accessDeniedHandler(customAccessDeniedHandler))
			.addFilterBefore(userAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.userDetailsService(userDetailsService)
			.logout(logout -> logout
				.logoutUrl("/auth/signout")
				.addLogoutHandler(customLogoutHandler)
				.deleteCookies("JSESSIONID")
			);
		userAuthenticationFilter.setAuthenticationManager(authManager);

		return httpSecurity.build();
	}

	@Bean
	public AuthenticationManager authenticationManager() throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder
			= new AuthenticationManagerBuilder(objectPostProcessor);
		authenticationManagerBuilder
			.userDetailsService(userDetailsService);

		return authenticationManagerBuilder.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
