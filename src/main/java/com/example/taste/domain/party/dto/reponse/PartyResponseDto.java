package com.example.taste.domain.party.dto.reponse;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.PartyStatus;

@Getter
public class PartyResponseDto {
	private Long storeId;            // TODO: Store 통째로 넘겨주는게 좋을지, 아니면 필요한 정보만?
	private PartyStatus partyStatus;
	private String storeName;
	private LocalDateTime meetingTime;
	private int maxMembers;
	private int nowMembers;
	private boolean enableRandomMatching;

	@Builder
	public PartyResponseDto(Party party) {
		this.storeId = party.getStore() != null ? party.getStore().getId() : null;
		this.partyStatus = party.getPartyStatus();
		this.storeName = party.getStore() != null ? party.getStore().getName() : null;
		this.meetingTime = party.getMeetingTime() != null ? party.getMeetingTime() : null;
		this.maxMembers = party.getMaxMembers();
		// TODO: this.nowMembers = party; 인원 수 구해오는 로직 필요, 혹은 파라미터로 전달
		this.enableRandomMatching = party.isEnableRandomMatching();
	}
}
