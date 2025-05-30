package com.example.taste.domain.party.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;

import com.example.taste.domain.party.enums.PartyStatus;

@Entity
@Getter
@Table(name = "party")        // TODO: BaseCreatedEntity 상속
public class Party {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// TODO: 맛집, 유저(파티장) 연관관계
	@Column(nullable = false)
	private String title;
	private String description;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private PartyStatus partyStatus;
	private LocalDateTime meetingTime;

	@Column(nullable = false)
	private int maxMembers;
	@Column(nullable = false)
	private boolean enableRandomMatching = false;
}
