package com.example.taste.domain.image.dto;

import com.example.taste.domain.image.entity.Image;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ImageResponseDto {

	private String imgUrl;
	private String imgKey;

	@Builder
	public ImageResponseDto(Image image) {
		this.imgUrl = image.getUrl();
		this.imgKey = image.getUploadFileName();
	}

	public ImageResponseDto(String imgUrl, String imgKey) {
		this.imgUrl = imgUrl;
		this.imgKey = imgKey;
	}
}
