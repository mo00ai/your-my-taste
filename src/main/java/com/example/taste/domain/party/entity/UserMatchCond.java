package com.example.taste.domain.party.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;

import com.example.taste.domain.party.enums.MatchingStatus;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.enums.Gender;

@Entity        // TODO: 생성일만 있는거 상속
@Getter
@Table(name = "user_match_cond")
public class UserMatchCond {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private int ageMinRange;
	private int ageMaxRange;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	private String region;

	@Enumerated(EnumType.STRING)
	private MatchingStatus matchingStatus;

	// TODO: 가게, 카테고리 ManyToMany 연관 추가
}
