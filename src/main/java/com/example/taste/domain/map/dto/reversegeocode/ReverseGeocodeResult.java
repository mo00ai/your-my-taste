package com.example.taste.domain.map.dto.reversegeocode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReverseGeocodeResult {
	private String name;       // "legalcode", "admcode", "addr", "roadaddr"
	private ReverseGeocodeCode code;
	private ReverseGeocodeRegion region;
	private ReverseGeocodeLand land;         // "addr" 및 "roadaddr"에만 존재
}
