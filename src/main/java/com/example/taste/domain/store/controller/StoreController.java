package com.example.taste.domain.store.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.store.dto.response.StoreResponse;
import com.example.taste.domain.store.service.StoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {
	private final StoreService storeService;

	@GetMapping("/{id}")
	public CommonResponse<StoreResponse> getStore(@PathVariable Long id) {
		return CommonResponse.ok(storeService.getStore(id));
	}
}
