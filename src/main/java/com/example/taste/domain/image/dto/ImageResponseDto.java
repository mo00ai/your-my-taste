package com.example.taste.domain.image.dto;

import lombok.Getter;

@Getter
public class ImageResponseDto {

	private Long id;
	private String imgUrl;
	private String imgKey;

	// @Builder
	// public ImageResponseDto(Image image) {
	// 	this.id = image.getId();
	// 	this.imgUrl = image.getUrl();
	// 	this.imgKey = image.getUploadFileName();
	// }

	public ImageResponseDto(Long id, String imgUrl, String imgKey) {
		this.imgUrl = imgUrl;
		this.imgKey = imgKey;
	}
}
