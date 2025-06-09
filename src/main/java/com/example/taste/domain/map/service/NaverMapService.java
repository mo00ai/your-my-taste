package com.example.taste.domain.map.service;

import static com.example.taste.domain.map.exception.MapErrorCode.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.config.NaverConfig;
import com.example.taste.domain.map.dto.geocode.GeoMapDetailResponse;
import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeDetailResponse;

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
			URI uri = UriComponentsBuilder
				.fromUriString(naverConfig.getGeoCoding().getBaseUrl())
				.queryParam("query", address)
				.encode(StandardCharsets.UTF_8)            // 인코딩 UTF_8 설정
				.build()
				.toUri();        // 기존 toUriString과 String에서 -> toUrl와 URI 객체로 변경

			return webClient.get()
				.uri(uri)
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

	/**
	 * orders 변환 타입 파라미터
	 * 	legalcode:법정동으로 변환
	 * 	admcode:  행정동으로 변환
	 * 	addr:	 지번 주소로 변환
	 * 	roadaddr: 도로명 주소로 변환
	 * @return
	 */
	// coordinates -> address
	public ReverseGeocodeDetailResponse getAddressFromCoordinates(double longitude, double latitude) {
		String coords = longitude + "," + latitude;
		URI uri = UriComponentsBuilder
			.fromUriString(naverConfig.getReverseGeoCoding().getBaseUrl())
			.queryParam("coords", coords)            // 좌표
			.queryParam("orders", "legalcode,admcode,addr,roadaddr")    // 변환 타입
			.queryParam("output", "json")    // 응답 결과의 포맷 유형 JSON
			.encode(StandardCharsets.UTF_8)            // 인코딩 UTF_8 설정
			.build()
			.toUri();        // 기존 toUriString과 String에서 -> toUrl와 URI 객체로 변경

		return webClient.get()
			.uri(uri)
			// naver api spec
			.header("x-ncp-apigw-api-key-id", naverConfig.getClientId())
			.header("x-ncp-apigw-api-key", naverConfig.getClientSecret())
			.retrieve()
			.bodyToMono(ReverseGeocodeDetailResponse.class)
			.timeout(Duration.ofSeconds(10))
			.block();
	}

}
