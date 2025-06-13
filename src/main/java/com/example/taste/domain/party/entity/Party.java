package com.example.taste.domain.party.entity;

import java.time.LocalDate;
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
import lombok.Setter;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.example.taste.common.entity.BaseCreatedAtEntity;
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

	@Setter
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private PartyStatus partyStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	private LocalDate meetingDate;

	@Column(nullable = false)
	private int maxMembers;
	private int nowMembers = 0;
	@Column(nullable = false)
	private boolean enableRandomMatching = false;

	@Builder
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
		this.partyStatus = PartyStatus.RECRUITING;
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
			this.maxMembers = requestDto.getMaxMembers();    // MEMO: 근데 이거 변경할때 invitation 도 안바뀌게 락 걸어야하나 - @윤예진
		}
		if (requestDto.getEnableRandomMatching() != null) {
			this.enableRandomMatching = requestDto.getEnableRandomMatching();
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
			this.maxMembers = requestDto.getMaxMembers();    // MEMO: 근데 이거 변경할때 invitation 도 안바뀌게 락 걸어야하나 - @윤예진
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

	public boolean isHostOfParty(Long hostId) {
		return this.getHostUser().getId().equals(hostId);
	}

	public double calculateAverageMemberAge() {
		return this.partyInvitationList.stream()
			.filter(pi -> pi.getInvitationStatus().equals(InvitationStatus.CONFIRMED))
			.mapToInt(pi -> pi.getUser().getAge())
			.average().orElse(0.0);
	}
}
