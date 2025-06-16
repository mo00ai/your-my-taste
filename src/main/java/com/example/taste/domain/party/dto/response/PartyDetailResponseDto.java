package com.example.taste.domain.party.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.user.dto.response.UserSimpleResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyDetailResponseDto {
	private Long partyId;
	private String title;
	private String description;
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
		this.partyId = party.getId();
		this.title = party.getTitle();
		this.description = party.getDescription();
		this.partyStatus = party.getPartyStatus().toString();
		this.storeId = party.getStore() != null ? party.getStore().getId() : null;
		this.storeName = party.getStore() != null ? party.getStore().getName() : null;
		this.meetingDate = party.getMeetingDate().format(
			DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		this.maxMembers = party.getMaxMembers();
		this.nowMembers = party.getNowMembers();            // 파티장 포함
		this.enableRandomMatching = party.isEnableRandomMatching();
		this.host = host;
		this.members = members;
	}
}
