package com.example.taste.domain.searchapi.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.searchapi.dto.NaverLocalSearchResponseDto;
import com.example.taste.domain.searchapi.service.SearchApiService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchApiController {

	private final SearchApiService naverSearchApiService;

	@GetMapping(produces = "application/json; charset=UTF-8")
	public CommonResponse<NaverLocalSearchResponseDto> searchKeywords(
		@RequestParam @NotBlank(message = "키워드는 필수값입니다") String query
	) {
		NaverLocalSearchResponseDto response = naverSearchApiService.searchLocal(query);
		return CommonResponse.ok(response);
	}

}
