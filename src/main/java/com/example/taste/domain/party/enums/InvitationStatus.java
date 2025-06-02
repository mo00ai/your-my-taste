package com.example.taste.domain.party.enums;

public enum InvitationStatus {
	WAITING,      // 대기 (신청·초대·랜덤 중간 단계 전부)
	CONFIRMED,    // 확정
	FAILED,       // 거절·만료·취소 등 실패
	EXITED,       // 매칭 이후 나감
	CLOSED        // 파티 해산
}
