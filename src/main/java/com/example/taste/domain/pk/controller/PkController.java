package com.example.taste.domain.pk.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pk")
@RequiredArgsConstructor
public class PkController {

	// private final PkService pkCriteriaService;
	//
	// @PreAuthorize("hasRole('ADMIN')")
	// @PostMapping("/criteria")
	// public CommonResponse<PkCriteriaResponseDto> savePkCriteria(@AuthenticationPrincipal CustomUserDetails userDetails,
	// 	@Valid @RequestBody PkSaveRequestDto dto) {
	//
	// 	return CommonResponse.created(pkCriteriaService.savePkCriteria(dto.getType(), dto.getPoint()));
	// }
	//
	// @GetMapping("/criteria")
	// public CommonResponse<List<PkCriteriaResponseDto>> findAllPkCriteria(
	// 	@AuthenticationPrincipal CustomUserDetails userDetails) {
	//
	// 	return CommonResponse.ok(pkCriteriaService.findAllPkCriteria());
	// }
	//
	// @PreAuthorize("hasRole('ADMIN')")
	// @PutMapping("/criteria/{id}")
	// public CommonResponse<Void> updatePkCriteria(@AuthenticationPrincipal CustomUserDetails userDetails,
	// 	@PathVariable Long id,
	// 	@Valid @RequestBody PkUpdateRequestDto dto) {
	//
	// 	pkCriteriaService.updatePkCriteria(id, dto);
	// 	return CommonResponse.ok();
	// }
	//
	// @PreAuthorize("hasRole('ADMIN')")
	// @DeleteMapping("/criteria/{id}")
	// public CommonResponse<Void> deletePkCriteria(@AuthenticationPrincipal CustomUserDetails userDetails,
	// 	@PathVariable Long id) {
	//
	// 	pkCriteriaService.deletePkCriteria(id);
	// 	return CommonResponse.ok();
	// }

}
