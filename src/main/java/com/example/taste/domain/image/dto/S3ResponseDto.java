package com.example.taste.domain.image.dto;

import lombok.Getter;

@Getter
public class S3ResponseDto {
	private final String staticUrl;
	private final String uploadFileName;
	private final String originalFileName;

	public S3ResponseDto(String staticUrl, String uploadFileName, String originalFileName) {
		this.staticUrl = staticUrl;
		this.uploadFileName = uploadFileName;
		this.originalFileName = originalFileName;
	}

}
