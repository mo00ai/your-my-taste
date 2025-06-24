package com.example.taste.domain.pk.enums;

public enum PkType {
	POST(30),
	LIKE(20),
	REVIEW(20),
	EVENT(500),
	RESET(0); // 예: RESET은 점수 없음

	private final int point;

	PkType(int point) {
		this.point = point;
	}

	public int getPoint() {
		return point;
	}
}
