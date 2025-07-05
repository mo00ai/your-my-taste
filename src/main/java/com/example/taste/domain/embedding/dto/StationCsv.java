package com.example.taste.domain.embedding.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StationCsv {
	private String name;          // 역명
	private String line;          // 호선명
	private String latitude;      // 위도
	private String longitude;     // 경도
}
