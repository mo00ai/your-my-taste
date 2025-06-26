package com.example.taste.domain.recommend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.recommend.dto.request.RecommendRequestDto;
import com.example.taste.domain.recommend.dto.response.RecommendResponseDto;
import com.example.taste.domain.recommend.service.RecommendService;
import com.example.taste.domain.user.entity.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommends")
public class RecommendController {

	private final RecommendService recommendService;

	@PostMapping
	public Mono<CommonResponse<RecommendResponseDto>> recommend(@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody(required = false) RecommendRequestDto dto) {
		return recommendService.recommend(userDetails.getId(), dto)
			.map(CommonResponse::ok);
	}

}
