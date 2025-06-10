package com.example.taste.domain.party.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.example.taste.common.entity.BaseCreatedAtEntity;
import com.example.taste.domain.party.dto.request.PartyCreateRequestDto;
import com.example.taste.domain.party.dto.request.PartyDetailUpdateRequestDto;
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

	@OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PartyInvitation> partyInvitationList;

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
	private int nowMembers = 0;
	@Column(nullable = false)
	private boolean enableRandomMatching = false;

	@Builder
	public Party(User hostUser, List<PartyInvitation> partyInvitationList, String title,
		String description, PartyStatus partyStatus, Store store,
		LocalDateTime meetingTime, int maxMembers, int nowMembers, Boolean enableRandomMatching) {
		this.hostUser = hostUser;
		this.partyInvitationList = partyInvitationList;
		this.title = title;
		this.description = description;
		this.partyStatus = partyStatus;
		this.store = store;
		this.meetingTime = meetingTime;
		this.maxMembers = maxMembers;
		this.nowMembers = nowMembers;
		this.enableRandomMatching = enableRandomMatching != null ? enableRandomMatching : false;
	}

	@Builder
	public Party(PartyCreateRequestDto requestDto, User hostUser) {
		this.hostUser = hostUser;
		this.title = requestDto.getTitle();
		this.description =
			requestDto.getDescription() != null ? requestDto.getDescription() : null;
		this.meetingTime =
			requestDto.getMeetingTime() != null ? requestDto.getMeetingTime() : null;
		this.maxMembers =
			requestDto.getMaxMembers() != null ? requestDto.getMaxMembers() : null;
		this.enableRandomMatching =
			requestDto.getEnableRandomMatching() != null ? requestDto.getEnableRandomMatching() : null;
		this.partyStatus = PartyStatus.RECRUITING;
	}

	@Builder
	public Party(PartyCreateRequestDto requestDto, User hostUser, Store store) {
		this.hostUser = hostUser;
		this.store = store;
		this.title = requestDto.getTitle();
		this.description =
			requestDto.getDescription() != null ? requestDto.getDescription() : null;
		this.meetingTime =
			requestDto.getMeetingTime() != null ? requestDto.getMeetingTime() : null;
		this.maxMembers =
			requestDto.getMaxMembers() != null ? requestDto.getMaxMembers() : null;
		this.nowMembers = 1;
		this.enableRandomMatching =
			requestDto.getEnableRandomMatching() != null ? requestDto.getEnableRandomMatching() : null;
		this.partyStatus = PartyStatus.RECRUITING; // TODO: 이것도 여러가지 상황 체크 필요 (만약 생성하자마자 약속 시간 지났다면)
	}

	public void update(PartyDetailUpdateRequestDto requestDto, Store store) {
		this.store = store;
		if (requestDto.getTitle() != null) {
			this.title = requestDto.getTitle();
		}
		if (requestDto.getDescription() != null) {
			this.description = requestDto.getDescription();
		}
		if (requestDto.getMeetingTime() != null) {
			this.meetingTime = requestDto.getMeetingTime();
		}
		if (requestDto.getMaxMembers() != null) {
			this.maxMembers = requestDto.getMaxMembers();    //TODO: 근데 이거 변경할때 invitation 도 안바뀌게 락 걸어야하나?
		}
		if (requestDto.getEnableRandomMatching() != null) {
			this.enableRandomMatching = requestDto.getEnableRandomMatching();
		}
	}

	public void update(PartyDetailUpdateRequestDto requestDto) {
		if (requestDto.getTitle() != null) {
			this.title = requestDto.getTitle();
		}
		if (requestDto.getDescription() != null) {
			this.description = requestDto.getDescription();
		}
		if (requestDto.getMeetingTime() != null) {
			this.meetingTime = requestDto.getMeetingTime();
		}
		if (requestDto.getMaxMembers() != null) {
			this.maxMembers = requestDto.getMaxMembers();    //TODO: 근데 이거 변경할때 invitation도 안바뀌게 락 걸어야하나?
		}
		if (requestDto.getEnableRandomMatching() != null) {
			this.enableRandomMatching = requestDto.getEnableRandomMatching();
		}
	}

	public void joinMember() {
		this.nowMembers++;
	}

	public void leaveMember() {
		this.nowMembers--;
	}

	public boolean isFull() {
		return this.partyStatus.equals(PartyStatus.FULL) || (this.nowMembers >= this.maxMembers);
	}
}
