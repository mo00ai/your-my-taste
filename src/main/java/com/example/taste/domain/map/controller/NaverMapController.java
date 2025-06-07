package com.example.taste.domain.map.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.NaverConfig;
import com.example.taste.domain.map.dto.geo.GeoMapDetailResponse;
import com.example.taste.domain.map.service.NaverMapService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class NaverMapController {
	private final NaverMapService naverMapService;
	private final NaverConfig naverConfig;

	// 주소로 좌표 검색
	@GetMapping("/geocoding/string")
	public CommonResponse<String> getCoordinate(@RequestParam String address) {
		String coordinates = "(" + naverMapService.getCoordinatesFromAddress(address) + ")";
		return CommonResponse.ok(coordinates);
	}

	// 주소로 좌표 검색
	@GetMapping("/geocoding")
	public CommonResponse<GeoMapDetailResponse> getCoordinates(@RequestParam String address) {
		GeoMapDetailResponse response = naverMapService.getCoordinatesFromAddress(address);
		return CommonResponse.ok(response);
	}

	// 좌표로 주소 검색
	@GetMapping("reverse-geocoding")
	public CommonResponse<GeoMapDetailResponse> getAddress(
		@RequestParam double longitude,
		@RequestParam double latitude
	) {
		// try {
		GeoMapDetailResponse response = naverMapService.getAddressFromCoordinates(longitude, latitude);
		return CommonResponse.ok(response);
		// } catch (Exception e) {
		// 	// return CommonResponse.error()
		// 		// ResponseEntity.badRequest().body("좌표 검색 실패!");
		// }

	}
/*
	@GetMapping("/test-config")
	public CommonResponse<String> testConfig() {
		log.info("=== 환경변수 직접 확인 ===");
		log.info("System.getenv CLIENT_ID: [{}]", System.getenv("NAVER_MAP_CLIENT_ID"));
		log.info("System.getenv CLIENT_SECRET: [{}]", System.getenv("NAVER_MAP_CLIENT_SECRET"));
		log.info("NaverConfig null 여부: {}", naverConfig == null);

		if (naverConfig != null) {
			log.info("NaverConfig CLIENT_ID: [{}]", naverConfig.getClientId());
			log.info("NaverConfig CLIENT_SECRET: [{}]", naverConfig.getClientSecret());
		}
		log.info("========================");

		return CommonResponse.ok("Check logs");
	}

 */
}
