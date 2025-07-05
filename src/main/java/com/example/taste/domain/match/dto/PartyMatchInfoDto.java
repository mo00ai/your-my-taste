package com.example.taste.domain.match.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.match.entity.AgeRange;
import com.example.taste.domain.match.entity.PartyMatchInfo;
import com.example.taste.domain.match.entity.PartyMatchInfoFavor;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.user.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PartyMatchInfoDto implements Serializable {
	private Long id;
	private Long partyId;
	private Long hostId;
	private Long storeId;
	private Long storeCategoryId;
	private LocalDate meetingDate;
	private String region;
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private Gender prefGender;
	private AgeRange ageRange;
	private Double avgAge;
	private MatchStatus matchStatus;
	private List<Favor> favorList;

	@Builder
	public PartyMatchInfoDto(PartyMatchInfo entity, Double avgAge) {
		this.id = entity.getId();
		this.partyId = entity.getParty().getId();
		this.hostId = entity.getParty().getHostUser().getId();
		this.storeId = entity.getStore() != null ? entity.getStore().getId() : null;
		this.storeCategoryId = entity.getStore() != null ? entity.getStore().getCategory().getId() : null;
		this.meetingDate = entity.getMeetingDate();
		this.region = entity.getRegion();
		this.prefGender = entity.getGender();
		this.ageRange = entity.getAgeRange();
		this.matchStatus = entity.getMatchStatus();
		this.favorList = entity.getFavorList() != null ?
			entity.getFavorList().stream()
				.map(PartyMatchInfoFavor::getFavor)
				.toList()
			: List.of();
		this.avgAge = avgAge;
	}

	@Override
	public String toString() {
		return "PartyMatchInfoDto{" +
			"id=" + id +
			", partyId=" + partyId +
			", storeId=" + storeId +
			", meetingDate=" + meetingDate +
			", region='" + region + '\'' +
			", gender=" + prefGender +
			", ageRange=" + ageRange +
			", matchStatus=" + matchStatus +
			", favorList=" + favorList +
			", averageAge=" + avgAge +
			'}';
	}

	@Builder(buildMethodName = "buildWithNewAvgAge", builderMethodName = "builderWithNewAvgAge")
	public PartyMatchInfoDto(PartyMatchInfoDto dto, Double newAvgAge) {
		this.id = dto.getId();
		this.partyId = dto.getPartyId();
		this.hostId = dto.getHostId();
		this.storeId = dto.getStoreId() != null ? dto.getStoreId() : null;
		this.storeCategoryId = dto.getStoreCategoryId() != null ? dto.getStoreCategoryId() : null;
		this.meetingDate = dto.getMeetingDate();
		this.region = dto.getRegion();
		this.prefGender = dto.getPrefGender();
		this.ageRange = dto.getAgeRange();
		this.matchStatus = dto.getMatchStatus();
		this.favorList = dto.getFavorList() != null ? dto.getFavorList() : null;
		this.avgAge = newAvgAge;
	}
}
