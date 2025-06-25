package com.example.taste.domain.recommend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CoordinateResponseDto {

	private double lon; // 경도
	private double lat; // 위도

	@Builder
	public CoordinateResponseDto(double lon, double lat) {
		this.lon = lon;
		this.lat = lat;
	}
}
