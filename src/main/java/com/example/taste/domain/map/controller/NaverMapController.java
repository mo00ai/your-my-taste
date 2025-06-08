package com.example.taste.domain.map.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.map.dto.geocode.GeoMapDetailResponse;
import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeDetailResponse;
import com.example.taste.domain.map.service.NaverMapService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class NaverMapController {
	private final NaverMapService naverMapService;

	// 주소로 좌표 검색
	@GetMapping("/geocoding")
	public CommonResponse<GeoMapDetailResponse> getCoordinates(@RequestParam String address) {
		GeoMapDetailResponse response = naverMapService.getCoordinatesFromAddress(address);
		return CommonResponse.ok(response);
	}

	// 좌표로 주소 검색
	@GetMapping("reverse-geocoding")
	public CommonResponse<ReverseGeocodeDetailResponse> getAddress(
		@RequestParam double longitude,    // 경도	x축
		@RequestParam double latitude    //	  위도	y축
	) {
		ReverseGeocodeDetailResponse response = naverMapService.getAddressFromCoordinates(longitude, latitude);
		return CommonResponse.ok(response);

	}
}
