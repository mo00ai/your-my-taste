package com.example.taste.domain.recommend.service;

import static com.example.taste.domain.recommend.exception.RecommendErrorCode.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.recommend.dto.response.AddressResponseDto;
import com.example.taste.domain.recommend.dto.response.CoordinateResponseDto;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AddressService {

	@Value("${kakao.rest.api.key}")
	private String kakaoKey;

	private final WebClient kakaoWebClient;

	public Mono<CoordinateResponseDto> getCoordinates(User user) {

		Mono<CoordinateResponseDto> addressResult = kakaoWebClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/v2/local/search/address.json")
				.queryParam("query", user.getAddress())
				.build())
			.header("Authorization", "KakaoAK " + kakaoKey)
			.retrieve()
			.bodyToMono(AddressResponseDto.class)
			.map(response -> response.getDocuments().stream()
				.findFirst()
				.map(document -> CoordinateResponseDto.builder()
					.lon(Double.parseDouble(document.getX()))
					.lat(Double.parseDouble(document.getY()))
					.build())
				.orElseThrow(() -> new CustomException(ADDRESS_LOAD_FAILED))
			);

		return addressResult;
	}
}
