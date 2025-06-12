package com.example.taste.domain.image.enums;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum ImageExtension {
	JPG("jpg", "image/jpeg"),
	JPEG("jpeg", "image/jpeg"),
	PNG("png", "image/png");

	private final String extension;
	private final String mimeType;

	ImageExtension(String extension, String mimeType) {
		this.extension = extension;
		this.mimeType = mimeType;
	}

	public static boolean isValidExtension(String extension) {
		return Arrays.stream(values()).anyMatch(image -> image.extension.equalsIgnoreCase(extension));
	}

	public static boolean isValidMimeType(String mimeType) {
		return Arrays.stream(values()).anyMatch(image -> image.mimeType.equalsIgnoreCase(mimeType));
	}

}
