package com.example.taste.domain.map.dto.geo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *	addressElements: 주소 구성 요소 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GeoAddressElements {
	/**
	 * types: 주소 구성 요소 타입
	 * SIDO | SIGUGUN | DONGMYUN | RI | ROAD_NAME | BUILDING_NUMBER | BUILDING_NAME | LAND_NUMBER | POSTAL_CODE
	 * SIDO: 시/도
	 * SIGUGUN: 시/구/군
	 * DONGMYUN: 동/면
	 * RI: 리
	 * ROAD_NAME: 도로명
	 * BUILDING_NUMBER: 건물 번호
	 * BUILDING_NAME: 건물 이름
	 * LAND_NUMBER: 번지
	 * POSTAL_CODE: 우편번호
	 */
	private List<String> types;
	/**
	 * longName: 주소 구성 요소 이름
	 */
	private String longName;
	/**
	 * 	shortName: 주소 구성 요소 축약 이름
	 */
	private String shortName;

	private String code;
}
