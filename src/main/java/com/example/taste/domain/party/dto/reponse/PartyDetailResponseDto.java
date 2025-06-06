package com.example.taste.domain.party.dto.reponse;

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
	private String meetingTime;
	private int maxMembers;
	private int nowMembers;
	private boolean enableRandomMatching;
	private UserSimpleResponseDto host;
	private List<UserSimpleResponseDto> members;

	// TODO: 이렇게 전달할지, 아니면 그냥 유저 리스트로 다 담아버리고 호스트 인덱스 알려주는 필드를 추가해서 알려줄지
	@Builder
	public PartyDetailResponseDto(
		Party party, UserSimpleResponseDto host, List<UserSimpleResponseDto> members) {
		this.partyStatus = party.getPartyStatus().toString();
		this.storeId = party.getStore().getId();
		this.storeName = party.getStore().getName();
		this.meetingTime = party.getMeetingTime().format(
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		this.maxMembers = party.getMaxMembers();
		this.nowMembers = party.getNowMembers();            // 파티장 포함
		this.enableRandomMatching = party.isEnableRandomMatching();
		this.host = host;
		this.members = members;
	}
}
