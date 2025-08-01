package com.example.taste.domain.map.dto.geocode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoMeta {
	/**
	 * meta.totalCount: 응답 결과 개수
	 */
	private int totalCount;
	/**
	 * meta.page: 현재 페이지 번호
	 */
	private int page;
	/**
	 * meta.count: 페이지 내 결과 개수
	 */
	private int count;

}
