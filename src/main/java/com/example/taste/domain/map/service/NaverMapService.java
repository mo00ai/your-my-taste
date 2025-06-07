package com.example.taste.domain.map.service;

import static com.example.taste.domain.map.exception.MapErrorCode.*;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.config.NaverConfig;
import com.example.taste.domain.map.dto.geo.GeoMapDetailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class NaverMapService {

	private final WebClient webClient;
	private final NaverConfig naverConfig;

	// address -> coordinates
	public GeoMapDetailResponse getCoordinatesFromAddress(String address) {
		try {
			String url = UriComponentsBuilder
				.fromUriString(naverConfig.getGeoCoding().getBaseUrl())
				.queryParam("query", address)
				.encode()
				.toUriString();

			return webClient.get()
				.uri(url)
				// naver api spec
				.header("x-ncp-apigw-api-key-id", naverConfig.getClientId())
				.header("x-ncp-apigw-api-key", naverConfig.getClientSecret())
				.header("Accept", "application/json")
				.retrieve()
				.bodyToMono(GeoMapDetailResponse.class)
				.timeout(Duration.ofSeconds(10))
				.block();

		} catch (WebClientResponseException e) {
			throw new CustomException(GEOCODING_API_ERROR);
		} catch (WebClientException e) {
			throw new CustomException(GEOCODING_API_ERROR);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

	}

	// address -> coordinates test용
	public String getCoordinatesFromAddress1(String address) {
		try {
			String url = UriComponentsBuilder
				.fromUriString(naverConfig.getGeoCoding().getBaseUrl())
				.queryParam("query", address)
				.encode()
				.toUriString();

			return webClient.get()
				.uri(url)
				// naver api spec
				.header("x-ncp-apigw-api-key-id", naverConfig.getClientId())
				.header("x-ncp-apigw-api-key", naverConfig.getClientSecret())
				.header("Accept", "application/json")
				.retrieve()
				.bodyToMono(String.class)
				.timeout(Duration.ofSeconds(10))
				.block();

		} catch (WebClientResponseException e) {
			throw new CustomException(GEOCODING_API_ERROR);
		} catch (WebClientException e) {
			throw new CustomException(GEOCODING_API_ERROR);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

	}

	// coordinates -> address
	public GeoMapDetailResponse getAddressFromCoordinates(double longitude, double latitude) {
		String coords = longitude + "," + latitude;
		String uri = UriComponentsBuilder
			.fromUriString(naverConfig.getReverseGeoCoding().getBaseUrl())
			.queryParam("coords", coords)
			.queryParam("output", "json")
			.queryParam("orders", "legalcode,admcode,addr,roadaddr")
			.toUriString();

		return webClient.get()
			.uri(uri)
			// naver api spec
			.header("x-ncp-apigw-api-key-id", naverConfig.getClientId())
			.header("x-ncp-apigw-api-key", naverConfig.getClientSecret())
			.retrieve()
			.bodyToMono(GeoMapDetailResponse.class)
			.block();
	}

}
