package com.example.taste.domain.map.dto.reversegeocode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 각 area 정보
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReverseGeocodeArea {
	private String name;
	private String alias;   // admcode.area1에만, 그 외엔 null
	private ReverseGeocodeCoords coords;
}
