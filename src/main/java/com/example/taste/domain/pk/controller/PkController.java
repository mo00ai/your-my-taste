package com.example.taste.domain.pk.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.pk.dto.request.PkSaveRequestDto;
import com.example.taste.domain.pk.dto.request.PkUpdateRequestDto;
import com.example.taste.domain.pk.dto.response.PkCriteriaResponseDto;
import com.example.taste.domain.pk.service.PkService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pk")
@RequiredArgsConstructor
public class PkController {

	private final PkService pkCriteriaService;

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/criteria")
	public CommonResponse<PkCriteriaResponseDto> savePkCriteria(@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody PkSaveRequestDto dto) {

		return CommonResponse.created(pkCriteriaService.savePkCriteria(dto.getType(), dto.getPoint()));
	}

	@GetMapping("/criteria")
	public CommonResponse<List<PkCriteriaResponseDto>> findAllPkCriteria(
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.ok(pkCriteriaService.findAllPkCriteria());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/criteria/{id}")
	public CommonResponse<Void> updatePkCriteria(@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long id,
		@Valid @RequestBody PkUpdateRequestDto dto) {

		pkCriteriaService.updatePkCriteria(id, dto);
		return CommonResponse.ok();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/criteria/{id}")
	public CommonResponse<Void> deletePkCriteria(@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long id) {

		pkCriteriaService.deletePkCriteria(id);
		return CommonResponse.ok();
	}

}
