package com.example.taste.domain.image.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

	public String getExtension() {
		return extension;
	}

	public String getMimeType() {
		return mimeType;
	}

	public static boolean isValidExtension(String ext) {
		return Arrays.stream(values()).anyMatch(t -> t.extension.equalsIgnoreCase(ext));
	}

	public static boolean isValidMimeType(String mime) {
		return Arrays.stream(values()).anyMatch(t -> t.mimeType.equalsIgnoreCase(mime));
	}

	public static List<String> getAllExtensions() {
		return Arrays.stream(values()).map(ImageExtension::getExtension).collect(Collectors.toList());
	}

	public static List<String> getAllMimeTypes() {
		return Arrays.stream(values()).map(ImageExtension::getMimeType).collect(Collectors.toList());
	}
}
