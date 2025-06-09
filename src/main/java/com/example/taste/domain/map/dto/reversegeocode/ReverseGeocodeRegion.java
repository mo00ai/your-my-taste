package com.example.taste.domain.map.dto.reversegeocode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReverseGeocodeRegion {
	private ReverseGeocodeArea area0;
	private ReverseGeocodeArea area1;
	private ReverseGeocodeArea area2;
	private ReverseGeocodeArea area3;
	private ReverseGeocodeArea area4;
}
