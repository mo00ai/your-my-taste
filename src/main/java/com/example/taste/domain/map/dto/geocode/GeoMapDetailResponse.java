package com.example.taste.domain.map.dto.geocode;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoMapDetailResponse {
	private String status;    // 기존 GeoStatus에서 단일 String으로 변경
	private GeoMeta meta;
	private List<GeoAddress> addresses;
	private String errorMessage;
}
