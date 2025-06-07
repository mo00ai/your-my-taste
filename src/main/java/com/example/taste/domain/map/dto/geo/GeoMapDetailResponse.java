package com.example.taste.domain.map.dto.geo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GeoMapDetailResponse {
	private GeoStatus status;
	private GeoMeta meta;
	private List<GeoAddress> addresses;
	private String errorMessage;
}
