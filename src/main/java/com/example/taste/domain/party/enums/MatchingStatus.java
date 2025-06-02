package com.example.taste.domain.party.enums;

public enum MatchingStatus {
	IDLE,            // 매칭 안 하는 상태
	MATCHING,        // 조건 걸고 매칭 중
	WAITING_HOST,     // 유저를 찾았고 파티장 응답 대기
	WAITING_USER    // 파티장이 수락했고 유저 응답 대기
}
