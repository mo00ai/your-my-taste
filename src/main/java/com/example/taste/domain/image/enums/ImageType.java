package com.example.taste.domain.image.enums;

public enum ImageType {
	BOARD("Board", 5),
	REVIEW("Review", 1),
	USER("User", 1),
	DEFAULT("Default", 1);

	private final String className;
	private final int maxCount;

	ImageType(String className, int maxCount) {
		this.className = className;
		this.maxCount = maxCount;
	}

	public String getClassName() {
		return className;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public static ImageType fromControllerClass(Class<?> clazz) {
		String name = clazz.getSimpleName();
		for (ImageType type : values()) {
			if (name.contains(type.className)) {
				return type;
			}
		}
		return DEFAULT;
	}
}
