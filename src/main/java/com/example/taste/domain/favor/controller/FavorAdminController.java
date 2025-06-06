package com.example.taste.domain.favor.controller;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.favor.dto.request.FavorAdminRequestDto;
import com.example.taste.domain.favor.dto.response.FavorAdminResponseDto;
import com.example.taste.domain.favor.service.FavorAdminService;

@RestController
@RequestMapping("/admin/favor")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class FavorAdminController {
	private final FavorAdminService favorAdminService;

	@PostMapping
	public CommonResponse<Void> createFavor(
		@RequestBody @Valid List<@Valid FavorAdminRequestDto> requestDtoList) {
		favorAdminService.createFavor(requestDtoList);
		return CommonResponse.ok();
	}

	@GetMapping
	public CommonResponse<List<FavorAdminResponseDto>> getAllFavor() {
		return CommonResponse.ok(favorAdminService.getAllFavor());
	}

	@PatchMapping("/{favorId}")
	public CommonResponse<Void> updateFavor(
		@PathVariable Long favorId, @RequestBody @Valid FavorAdminRequestDto requestDto) {
		favorAdminService.updateFavor(favorId, requestDto);
		return CommonResponse.ok();
	}

	@DeleteMapping("/{favorId}")
	public CommonResponse<Void> deleteFavor(@PathVariable Long favorId) {
		favorAdminService.deleteFavor(favorId);
		return CommonResponse.ok();
	}
}
