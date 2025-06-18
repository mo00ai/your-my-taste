package com.example.taste.domain.image.dto;

public class S3ResponseDto {
	private final String url;
	private final String uploadFileName;
	private final String originalFileName;

	public S3ResponseDto(String url, String uploadFileName, String originalFileName) {
		this.url = url;
		this.uploadFileName = uploadFileName;
		this.originalFileName = originalFileName;
	}

	public String getUrl() {
		return url;
	}

	public String getUploadFileName() {
		return uploadFileName;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}
}
