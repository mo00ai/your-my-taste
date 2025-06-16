package com.example.taste.domain.party.dto.response;

import java.time.format.DateTimeFormatter;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.PartyStatus;

@Getter
public class PartyResponseDto {
	private Long partyId;
	private String title;
	private PartyStatus partyStatus;
	private Long storeId;
	private String storeName;
	private String meetingDate;
	private int maxMembers;
	private int nowMembers;
	private boolean enableRandomMatching;

	@Builder
	public PartyResponseDto(Party party) {
		this.partyId = party.getId();
		this.title = party.getTitle();
		this.partyStatus = party.getPartyStatus();
		this.storeId = party.getStore() != null ? party.getStore().getId() : null;
		this.storeName = party.getStore() != null ? party.getStore().getName() : null;
		this.meetingDate = party.getMeetingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		this.maxMembers = party.getMaxMembers();
		this.nowMembers = party.getNowMembers();
		this.enableRandomMatching = party.isEnableRandomMatching();
	}
}
