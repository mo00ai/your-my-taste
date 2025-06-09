package com.example.taste.domain.map.dto.reversegeocode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReverseGeocodeLand {
	private String type;
	private String number1;
	private String number2;
	private String name;             // roadaddr에만
	private ReverseGeocodeCoords coords;
	private ReverseGeocodeAddition addition0;
	private ReverseGeocodeAddition addition1;
	private ReverseGeocodeAddition addition2;
	private ReverseGeocodeAddition addition3;
	private ReverseGeocodeAddition addition4;
}
