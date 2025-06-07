package com.example.taste.domain.searchapi.service;

import static com.example.taste.domain.searchapi.exception.SearchErrorCode.*;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.taste.common.exception.CustomException;
import com.example.taste.config.NaverDatalabConfig;
import com.example.taste.domain.searchapi.dto.NaverLocalSearchResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class SearchApiService {

	private final WebClient webClient;
	private final NaverDatalabConfig naverDatalabConfig;

	/**
	 *
	 * @param keyword
	 * @queryParam display : 한 번에 표시할 검색 결과 개수(기본값: 1, 최댓값: 5)
	 * @queryParam start : 검색 시작 위치(기본값: 1, 최댓값: 1)
	 * @queryParam sort : 검색 결과 정렬 방법
	 * 					- random: 정확도순으로 내림차순 정렬(기본값)
	 * 					- comment: 업체 및 기관에 대한 카페, 블로그의 리뷰 개수순으로 내림차순 정렬
	 * @return
	 */
	public NaverLocalSearchResponseDto searchLocal(String keyword) {

		try {
			String uri = UriComponentsBuilder
				.fromUriString(naverDatalabConfig.getBaseUrl())
				.queryParam("query", keyword)
				.queryParam("display", 5)
				.queryParam("sort", "random")
				.encode()
				.toUriString();

			return webClient.get()
				.uri(uri)
				.header("X-Naver-Client-Id", naverDatalabConfig.getClientId())
				.header("X-Naver-Client-Secret", naverDatalabConfig.getClientSecret())
				.header("Accept", "application/json")
				.retrieve()
				.bodyToMono(NaverLocalSearchResponseDto.class)
				.timeout(Duration.ofSeconds(10))
				.block();

		} catch (WebClientResponseException e) {
			log.error("Naver Local Search API 응답 오류: {}", e.getResponseBodyAsString(), e);
			int status = e.getRawStatusCode();
			if (status == 400) {
				throw new CustomException(INCORRECT_QUERY_REQUEST);
			} else if (status == 404) {
				throw new CustomException(INVALID_SEARCH_API);
			} else {
				throw new CustomException(SYSTEM_ERROR);
			}

		}
	}
}
