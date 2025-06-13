package com.example.taste.domain.party.enums;

public enum InvitationStatus {
	WAITING,      // 대기 (신청·초대·랜덤 중간 단계 전부)
	CONFIRMED,    // 확정
	REJECTED,       // 거절 (초대 O, 가입 신청X )
	EXITED,       // 매칭 이후 나감 (초대 O, 가입 신청 O)
	KICKED,            // 강퇴 (초대 O, 가입 신청 X)
}
