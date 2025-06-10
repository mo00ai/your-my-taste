package com.example.taste.domain.party.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.store.dto.response.StoreSimpleResponseDto;

@Getter
public class PartySimpleResponseDto {
	private Long partyId;
	private String title;
	private int maxMembers;
	private int nowMembers;
	private StoreSimpleResponseDto store;

	@Builder
	public PartySimpleResponseDto(Party party) {
		this.partyId = party.getId();
		this.title = party.getTitle();
		this.maxMembers = party.getMaxMembers();
		this.nowMembers = party.getNowMembers();
		this.store = new StoreSimpleResponseDto(party.getStore());
	}
}
