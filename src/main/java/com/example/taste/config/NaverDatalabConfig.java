package com.example.taste.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "naver.datalab")
public class NaverDatalabConfig {
	private final String clientId;
	private final String clientSecret;
	private final String baseUrl;

}
