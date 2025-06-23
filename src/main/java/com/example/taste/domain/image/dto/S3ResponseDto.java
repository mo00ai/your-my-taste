package com.example.taste.domain.image.dto;

import lombok.Getter;

@Getter
public class S3ResponseDto {
	private final String preSignedUrl;
	private final String staticUrl;
	private final String uploadFileName;
	private final String originalFileName;

	public S3ResponseDto(String preSignedUrl, String staticUrl, String uploadFileName, String originalFileName) {
		this.preSignedUrl = preSignedUrl;
		this.staticUrl = staticUrl;
		this.uploadFileName = uploadFileName;
		this.originalFileName = originalFileName;
	}

}
