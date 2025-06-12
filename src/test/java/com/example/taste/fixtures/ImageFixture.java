package com.example.taste.fixtures;

import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;

public class ImageFixture {
	public static Image create() {
		return Image.builder()
			.type(ImageType.USER)
			.url("testUrl.png")
			.originFileName("originfile")
			.uploadFileName("uploadfile")
			.fileSize(500 * 500L)
			.fileExtension("png")
			.build();
	}
}
