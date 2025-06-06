package com.example.taste.domain.party.dto.request;

import jakarta.validation.constraints.Positive;

import lombok.Getter;

@Getter
public class PartyInvitationRequestDto {
	@Positive(message = "유효하지 않은 유저 ID 정보입니다.")
	private Long userId;
}
