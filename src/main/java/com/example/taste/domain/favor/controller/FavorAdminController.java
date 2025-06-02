package com.example.taste.domain.favor.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.favor.dto.request.FavorListCreateRequestDto;
import com.example.taste.domain.favor.dto.request.FavorRequestDto;
import com.example.taste.domain.favor.service.FavorAdminService;

// @HasRole("ADMIN") // TODO: 추후 권한 필터 추가
@RestController
@RequestMapping("/admin/favor")
@RequiredArgsConstructor
public class FavorAdminController {
	private final FavorAdminService favorAdminService;

	@PostMapping
	public CommonResponse<Void> createFavor(@RequestBody FavorListCreateRequestDto requestDto) {
		favorAdminService.createFavor(requestDto);
		return CommonResponse.ok();
	}

	@PatchMapping("/{favorId}")
	public CommonResponse<Void> updateFavor(@RequestBody FavorRequestDto requestDto) {
		favorAdminService.updateFavor(requestDto);
		return CommonResponse.ok();
	}
}
