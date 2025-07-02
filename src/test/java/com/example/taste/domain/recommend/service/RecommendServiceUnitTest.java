package com.example.taste.domain.recommend.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.taste.domain.recommend.dto.request.RecommendRequestDto;
import com.example.taste.domain.recommend.dto.response.RecommendResponseDto;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RecommendServiceUnitTest {

	@InjectMocks
	private RecommendService recommendService;

	@Test
	void fallbackMethod_shouldReturnDefaultMessage() {
		// given
		Long userId = 1L;
		RecommendRequestDto dto = new RecommendRequestDto("비 왔을 때 먹을 음식 추천해줘");

		// when
		Mono<RecommendResponseDto> result = recommendService.fallbackMethod(userId, dto, new RuntimeException());

		// then
		StepVerifier.create(result)
			.assertNext(res -> assertThat(res.getRecommend())
				.isEqualTo("현재 추천 서비스를 이용할 수 없습니다. 잠시 후 다시 시도해 주세요."))
			.verifyComplete();
	}
}
