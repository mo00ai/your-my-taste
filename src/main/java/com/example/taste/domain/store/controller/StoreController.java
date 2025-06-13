package com.example.taste.domain.store.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.searchapi.dto.NaverLocalSearchResponseDto;
import com.example.taste.domain.searchapi.service.SearchApiService;
import com.example.taste.domain.store.dto.response.StoreResponse;
import com.example.taste.domain.store.dto.response.StoreSimpleResponseDto;
import com.example.taste.domain.store.service.StoreService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {
	private final SearchApiService naverSearchApiService;
	private final StoreService storeService;

	@PostMapping
	public CommonResponse<StoreSimpleResponseDto> createStore(
		@RequestParam @NotBlank(message = "키워드는 필수값입니다") String query
	) {
		NaverLocalSearchResponseDto naverLocalSearchResponseDto = naverSearchApiService.searchLocal(query);
		StoreSimpleResponseDto responseDto = storeService.createStore(naverLocalSearchResponseDto);
		return CommonResponse.ok(responseDto);
	}

	@GetMapping("/{id}")
	public CommonResponse<StoreResponse> getStore(@PathVariable Long id) {
		return CommonResponse.ok(storeService.getStore(id));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public CommonResponse<Void> deleteStore(@PathVariable Long id) {
		storeService.deleteStore(id);
		return CommonResponse.ok();
	}
}
