package com.example.taste.domain.party.dto.request;

import lombok.Getter;

import com.example.taste.common.annotation.ValidEnum;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;

@Getter
public class InvitationActionRequestDto {
	@ValidEnum(message = "유효하지 않은 초대 타입입니다.", target = InvitationType.class)
	private String invitationType;

	@ValidEnum(message = "유효하지 않은 초대 상태 변경 값입니다.", target = InvitationStatus.class)
	private String invitationStatus;
}
