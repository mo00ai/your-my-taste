package com.example.taste.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "naver.map")
public class NaverConfig {

	private final String clientId;
	private final String clientSecret;
	private final Geocoding geoCoding;
	private final ReverseGeocoding reverseGeoCoding;

	@Getter
	@RequiredArgsConstructor
	public static class Geocoding {
		private final String baseUrl;
	}

	@Getter
	@RequiredArgsConstructor
	public static class ReverseGeocoding {
		private final String baseUrl;

	}
}

