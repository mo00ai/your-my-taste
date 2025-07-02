package com.example.taste.domain.map.dto.reversegeocode;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReverseGeocodeDetailResponse {
	private ReverseGeocodeStatus status;
	public List<ReverseGeocodeResult> results;
}
