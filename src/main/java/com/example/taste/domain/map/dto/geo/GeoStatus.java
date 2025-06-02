package com.example.taste.domain.map.dto.geo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeoStatus {
	/**
	 * TODO
	 * 	status	String	-	응답 코드
	 */
	private Integer code;

	private String name;

	private String message;
}
