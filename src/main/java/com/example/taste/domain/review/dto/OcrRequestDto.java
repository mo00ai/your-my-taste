package com.example.taste.domain.review.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OcrRequestDto {
	private String version;
	private String requestId;
	private Long timestamp;
	private List<Images> images;

	@Getter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Images {
		private String format;
		private String data;
		private String name;

	}
}
