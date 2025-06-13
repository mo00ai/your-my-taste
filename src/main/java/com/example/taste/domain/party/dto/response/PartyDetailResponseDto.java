package com.example.taste.domain.party.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.user.dto.response.UserSimpleResponseDto;

@Getter
public class PartyDetailResponseDto {
	private String partyStatus;
	private Long storeId;
	private String storeName;
	private String meetingDate;
	private int maxMembers;
	private int nowMembers;
	private boolean enableRandomMatching;
	private UserSimpleResponseDto host;
	private List<UserSimpleResponseDto> members;

	@Builder
	public PartyDetailResponseDto(
		Party party, UserSimpleResponseDto host, List<UserSimpleResponseDto> members) {
		this.partyStatus = party.getPartyStatus().toString();
		this.storeId = party.getStore().getId();
		this.storeName = party.getStore().getName();
		this.meetingDate = party.getMeetingDate().format(
			DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		this.maxMembers = party.getMaxMembers();
		this.nowMembers = party.getNowMembers();            // 파티장 포함
		this.enableRandomMatching = party.isEnableRandomMatching();
		this.host = host;
		this.members = members;
	}
}
