package com.example.taste.domain.party.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.example.taste.common.entity.BaseCreatedAtEntity;
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.user.entity.User;

@Entity
@Getter
@Table(name = "party")
public class Party extends BaseCreatedAtEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "user_id", nullable = false)
	private User hostUser;

	// TODO: 맛집 연관관계
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
