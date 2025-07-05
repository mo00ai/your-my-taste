package com.example.taste.domain.map.dto.geocode;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoAddress {
	/**
	 *	roadAddress: 도로명 주소
	 */
	private String roadAddress;
	/**
	 * jibunAddress: 지번 주소
	 */
	private String jibunAddress;
	/**
	 * 	AddressElements: 주소 구성 요소 정보
	 */
	private List<GeoAddressElements> addressElements;
	/**
	 * 	longitude: X 좌표(경도)
	 */
	@JsonProperty("x")
	private String longitude;
	/**
	 * latitue: Y 좌표(위도)
	 */
	@JsonProperty("y")
	private String latitude;

}
