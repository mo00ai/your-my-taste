package com.example.taste.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Getter;

@Component
@Getter
public class OpenAiConfig {

	@Value("${openai.api.key}")
	private String aiKey;

	@Bean
	public WebClient aiWebClient() {

		return WebClient.builder()
			.baseUrl("https://api.openai.com/v1")
			.defaultHeader("Authorization", "Bearer " + aiKey)
			.defaultHeader("Content-Type", "application/json")
			.build();
	}

}
