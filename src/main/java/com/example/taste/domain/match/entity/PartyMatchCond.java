package com.example.taste.domain.match.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.taste.common.entity.BaseCreatedAtEntity;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.MatchingStatus;
import com.example.taste.domain.user.enums.Gender;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "party_match_cond")
public class PartyMatchCond extends BaseCreatedAtEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	private Party party;

	private int ageMinRange;
	private int ageMaxRange;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	private String region;

	@Enumerated(EnumType.STRING)
	private MatchingStatus matchingStatus;

	@Builder
	public PartyMatchCond(Party party, int ageMinRange, int ageMaxRange, Gender gender, String region,
		MatchingStatus matchingStatus) {
		this.party = party;
		this.ageMinRange = ageMinRange;
		this.ageMaxRange = ageMaxRange;
		this.gender = gender;
		this.region = region;
		this.matchingStatus = matchingStatus;
	}
}
