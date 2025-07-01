package com.example.taste.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebClientConfig {

	@Bean
	public ConnectionProvider sharedConnectionProvider() {
		return ConnectionProvider.builder("sharedPool")
			.maxConnections(50)
			.maxIdleTime(Duration.ofMinutes(5))
			.maxLifeTime(Duration.ofMinutes(30))
			.pendingAcquireMaxCount(1000)
			.pendingAcquireTimeout(Duration.ofSeconds(60))
			.build();
	}

	@Bean
	public WebClient webClient() {
		return WebClient.builder().build();
	}

}
