package com.example.taste.domain.pk.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
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

	@PostMapping("/criteria")
	public CommonResponse<PkCriteriaResponseDto> savePkCriteria(@Valid @RequestBody PkSaveRequestDto dto) {

		return CommonResponse.created(pkCriteriaService.savePkCriteria(dto.getType(), dto.getPoint()));
	}

	@GetMapping("/criteria")
	public CommonResponse<List<PkCriteriaResponseDto>> findAllPkCriteria() {

		return CommonResponse.ok(pkCriteriaService.findAllPkCriteria());
	}

	@PutMapping("/criteria/{id}")
	public CommonResponse<Void> updatePkCriteria(@PathVariable Long id,
		@Valid @RequestBody PkUpdateRequestDto dto) {

		pkCriteriaService.updatePkCriteria(id, dto);
		return CommonResponse.ok();
	}

	@DeleteMapping("/criteria/{id}")
	public CommonResponse<Void> deletePkCriteria(@PathVariable Long id) {

		pkCriteriaService.deletePkCriteria(id);
		return CommonResponse.ok();
	}

}
