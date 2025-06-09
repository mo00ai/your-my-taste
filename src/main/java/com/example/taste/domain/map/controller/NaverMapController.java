package com.example.taste.domain.map.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.map.dto.geocode.GeoMapDetailResponse;
import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeDetailResponse;
import com.example.taste.domain.map.service.NaverMapService;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class NaverMapController {
	private final NaverMapService naverMapService;

	// 주소로 좌표 검색
	@GetMapping("/geocoding")
	public CommonResponse<GeoMapDetailResponse> getCoordinates(
		@RequestParam @NotBlank(message = "주소는 필수값입니다") String address
	) {
		GeoMapDetailResponse response = naverMapService.getCoordinatesFromAddress(address);
		return CommonResponse.ok(response);
	}

	// 좌표(소수점 이하 7자리까지)로 주소 검색
	@GetMapping("reverse-geocoding")
	public CommonResponse<ReverseGeocodeDetailResponse> getAddress(
		@RequestParam
		@NotNull(message = "경도(x)는 필수입니다")
		@DecimalMin(value = "124.0") @DecimalMax(value = "132.0")  // 경계 좌표 x좌표 124.5 <- -> 132.0
		Double longitude,    // 경도	x축
		@RequestParam
		@NotNull(message = "위도(y)는 필수입니다")
		@DecimalMin(value = "33.0") @DecimalMax(value = "43.0") // 경계 좌표 y좌표 33.0 <- -> 38.9
		Double latitude    //	  위도	y축
	) {
		ReverseGeocodeDetailResponse response = naverMapService.getAddressFromCoordinates(longitude, latitude);
		return CommonResponse.ok(response);

	}
}
