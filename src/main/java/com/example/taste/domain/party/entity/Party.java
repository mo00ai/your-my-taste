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

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.example.taste.common.entity.BaseCreatedAtEntity;
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "party")
public class Party extends BaseCreatedAtEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "user_id", nullable = false)
	private User hostUser;

	@Column(nullable = false)
	private String title;
	private String description;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private PartyStatus partyStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	private LocalDateTime meetingTime;

	@Column(nullable = false)
	private int maxMembers;
	@Column(nullable = false)
	private boolean enableRandomMatching = false;

	@Builder
	public Party(User hostUser, String title, String description, PartyStatus partyStatus, Store store,
		LocalDateTime meetingTime, int maxMembers, Boolean enableRandomMatching) {
		this.hostUser = hostUser;
		this.title = title;
		this.description = description;
		this.partyStatus = partyStatus;
		this.store = store;
		this.meetingTime = meetingTime;
		this.maxMembers = maxMembers;
		this.enableRandomMatching = enableRandomMatching != null ? enableRandomMatching : false;
	}
}
