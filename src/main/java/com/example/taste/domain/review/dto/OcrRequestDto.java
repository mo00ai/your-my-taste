package com.example.taste.domain.review.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class OcrRequestDto {
	private String version;
	private String requestId;
	private Long timestamp;
	private List<Images> images;

	public static class Images {
		private String format;
		private String data;
		private String name;

		@Builder
		public Images(String format, String data, String name) {
			this.format = format;
			this.data = data;
			this.name = name;
		}
	}

	@Builder
	public OcrRequestDto(String version, String requestId, Long timestamp, List<Images> images) {
		this.version = version;
		this.requestId = requestId;
		this.timestamp = timestamp;
		this.images = images;
	}
}
