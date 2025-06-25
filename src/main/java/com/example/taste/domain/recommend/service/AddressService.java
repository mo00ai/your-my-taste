package com.example.taste.domain.recommend.service;

import static com.example.taste.domain.recommend.exception.AiErrorCode.*;
import static com.example.taste.domain.user.exception.UserErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.recommend.dto.AddressResponseDto;
import com.example.taste.domain.recommend.dto.CoordinateResponseDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AddressService {

	private final WebClient webClient;
	private final UserRepository userRepository;

	public Mono<CoordinateResponseDto> getCoordinates(Long userId) {

		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_FOUND_USER));

		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/v2/local/search/address.json")
				.queryParam("query", user.getAddress())
				.build())
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
	}
}
