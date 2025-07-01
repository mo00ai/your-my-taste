package com.example.taste.domain.party.entity;

import java.time.LocalDate;
import java.util.List;

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

import com.example.taste.common.entity.SoftDeletableEntity;
import com.example.taste.domain.party.dto.request.PartyCreateRequestDto;
import com.example.taste.domain.party.dto.request.PartyUpdateRequestDto;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "party")
public class Party extends SoftDeletableEntity {
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
	@JoinColumn(name = "store_id", nullable = true)
	private Store store;

	private LocalDate meetingDate;

	@Column(nullable = false)
	private int maxMembers;
	private int nowMembers = 0;
	@Column(nullable = false)
	private boolean enableRandomMatching = false;

	@Builder(builderMethodName = "oDtoBuilder", buildMethodName = "buildDto")
	public Party(PartyCreateRequestDto requestDto, User hostUser, Store store) {
		this.hostUser = hostUser;
		this.title = requestDto.getTitle();
		this.description =
			requestDto.getDescription() != null ? requestDto.getDescription() : null;
		this.store = store;
		this.meetingDate =
			requestDto.getMeetingDate() != null ? requestDto.getMeetingDate() : null;
		this.nowMembers = 1;
		this.maxMembers =
			requestDto.getMaxMembers() != null ? requestDto.getMaxMembers() : 0;
		this.enableRandomMatching =
			requestDto.getEnableRandomMatching() != null ? requestDto.getEnableRandomMatching() : false;
		this.partyStatus = PartyStatus.ACTIVE;
	}

	@Builder(builderMethodName = "oPartyBuilder", buildMethodName = "buildParty")
	public Party(Long id, User hostUser, String title, String description,
		PartyStatus partyStatus, Store store, LocalDate meetingDate,
		int maxMembers, int nowMembers, boolean enableRandomMatching) {
		this.id = id;
		this.hostUser = hostUser;
		this.title = title;
		this.description = description;
		this.partyStatus = partyStatus;
		this.store = store;
		this.meetingDate = meetingDate;
		this.maxMembers = maxMembers;
		this.nowMembers = nowMembers;
		this.enableRandomMatching = enableRandomMatching;
	}

	public void update(PartyUpdateRequestDto requestDto, Store store) {
		this.store = store;
		if (requestDto.getTitle() != null) {
			this.title = requestDto.getTitle();
		}
		if (requestDto.getDescription() != null) {
			this.description = requestDto.getDescription();
		}
		if (requestDto.getMeetingDate() != null) {
			this.meetingDate = requestDto.getMeetingDate();
		}
		if (requestDto.getMaxMembers() != null) {
			this.maxMembers = requestDto.getMaxMembers();
		}
	}

	public void update(PartyUpdateRequestDto requestDto) {
		if (requestDto.getTitle() != null) {
			this.title = requestDto.getTitle();
		}
		if (requestDto.getDescription() != null) {
			this.description = requestDto.getDescription();
		}
		if (requestDto.getMeetingDate() != null) {
			this.meetingDate = requestDto.getMeetingDate();
		}
		if (requestDto.getMaxMembers() != null) {
			this.maxMembers = requestDto.getMaxMembers();
		}
	}

	public void joinMember() {
		this.nowMembers++;
	}

	public void leaveMember() {
		this.nowMembers--;
	}

	public boolean isFull() {
		return this.nowMembers == this.maxMembers;
	}

	public boolean isStatus(PartyStatus partyStatus) {
		return this.partyStatus.equals(partyStatus);
	}

	public boolean isHostOfParty(Long hostId) {
		return this.getHostUser().getId().equals(hostId);
	}

	public boolean isActiveMemberOfParty(Long userId, List<PartyInvitation> partyInvitationList) {
		return partyInvitationList.stream()
			.anyMatch(pi -> pi.getUser().getId().equals(userId)
				&& pi.getInvitationStatus().equals(InvitationStatus.CONFIRMED));
	}

	public double calculateAverageMemberAge(List<PartyInvitation> partyInvitationList) {
		return partyInvitationList.stream()
			.filter(pi -> pi.getInvitationStatus().equals(InvitationStatus.CONFIRMED))
			.mapToInt(pi -> pi.getUser().getAge())
			.average().orElse(0.0);
	}

	public double calculateAvgAgeAfterJoin(double oldAvgAge, int newMemberAge) {
		int oldMembersCount = this.nowMembers - 1;
		return (oldAvgAge * oldMembersCount + newMemberAge) / (oldMembersCount + 1);
	}

	public double calculateAvgAgeAfterLeave(double oldAvgAge, int leavedMemberAge) {
		int oldMembersCount = this.nowMembers + 1;
		return switch (this.nowMembers) {
			case 0 -> 0;
			case 1 -> this.hostUser.getAge();
			default -> (oldAvgAge * oldMembersCount - leavedMemberAge) / (oldMembersCount - 1);
		};
	}

	public void updateHost(User user) {
		this.hostUser = user;
	}

	public void updatePartyStatus(PartyStatus status) {
		this.partyStatus = status;
	}
}
