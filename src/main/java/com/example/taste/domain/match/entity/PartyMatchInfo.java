package com.example.taste.domain.match.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.example.taste.common.entity.BaseCreatedAtEntity;
import com.example.taste.domain.match.dto.request.PartyMatchInfoCreateRequestDto;
import com.example.taste.domain.match.vo.AgeRange;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.enums.Gender;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "party_match_info")
public class PartyMatchInfo extends BaseCreatedAtEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Party party;

	@OneToOne(optional = true)
	private Store store;
	private LocalDate meetingDate;

	@Embedded
	private AgeRange ageRange;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	private String region;

	@OneToMany(mappedBy = "partyMatchInfo", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PartyMatchInfoFavor> favorList;

	@Enumerated(EnumType.STRING)
	private MatchStatus matchStatus;

	@Builder
	public PartyMatchInfo(PartyMatchInfoCreateRequestDto requestDto, Party party) {
		this.party = party;
		this.store = party.getStore();
		this.meetingDate = party.getMeetingDate();
		this.ageRange = requestDto.getAgeRange();
		this.gender = requestDto.getGender() != null ? Gender.valueOf(requestDto.getGender()) : null;
		this.region = requestDto.getRegion();
		this.matchStatus = MatchStatus.MATCHING;
	}

	public void updateMatchStatus(MatchStatus matchStatus) {
		this.matchStatus = matchStatus;
	}

	public void updateFavorList(List<PartyMatchInfoFavor> favorList) {
		if (favorList != null) {
			this.favorList.clear();
			this.favorList.addAll(favorList);
		}
	}
}
