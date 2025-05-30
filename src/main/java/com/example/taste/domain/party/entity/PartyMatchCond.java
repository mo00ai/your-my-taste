package com.example.taste.domain.party.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Getter;

import com.example.taste.common.entity.BaseCreatedAtEntity;
import com.example.taste.domain.party.enums.MatchingStatus;
import com.example.taste.domain.user.enums.Gender;

@Entity
@Getter
@Table(name = "party_match_cond")
public class PartyMatchCond extends BaseCreatedAtEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(mappedBy = "party_id")
	private Party party;

	private int ageMinRange;
	private int ageMaxRange;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	private String region;

	@Enumerated(EnumType.STRING)
	private MatchingStatus matchingStatus;
}
