package com.example.taste.domain.party.dto.reponse;

import java.time.LocalDateTime;

import lombok.Getter;

import com.example.taste.domain.party.enums.PartyStatus;

@Getter
public class MyPartyResponseDto {
	private Long storeId;            // TODO: Store 통째로 넘겨주는게 좋을지, 아니면 필요한 정보만?
	private PartyStatus partyStatus;
	private String storeName;
	private LocalDateTime meetingTime;
	private int maxMembers;
	private int nowMembers;
	private boolean enableRandomMatching;
}
