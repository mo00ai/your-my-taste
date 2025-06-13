package com.example.taste.domain.match.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
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
import com.example.taste.domain.match.dto.request.UserMatchInfoCreateRequestDto;
import com.example.taste.domain.match.dto.request.UserMatchInfoUpdateRequestDto;
import com.example.taste.domain.match.vo.AgeRange;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.enums.Gender;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_match_info")
public class UserMatchInfo extends BaseCreatedAtEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private String title;

	@OneToMany(mappedBy = "userMatchInfo", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserMatchInfoStore> storeList;

	@OneToMany(mappedBy = "userMatchInfo", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserMatchInfoCategory> categoryList;

	@Embedded
	private AgeRange ageRange;
	private LocalDate meetingDate;
	private String region;

	@Enumerated(EnumType.STRING)
	private Gender userGender;
	private Integer userAge;

	@Enumerated(EnumType.STRING)
	private MatchStatus matchStatus;

	private LocalDateTime matchStartedAt;

	@Builder
	public UserMatchInfo(UserMatchInfoCreateRequestDto requestDto, User user) {
		if (user == null) {
			throw new IllegalArgumentException("필수 필드값이 누락되었습니다");
		}
		this.user = user;
		this.title = requestDto.getTitle();
		this.ageRange = requestDto.getAgeRange();
		this.meetingDate = requestDto.getMeetingDate() != null ? requestDto.getMeetingDate() : null;
		this.region = requestDto.getRegion();
		this.userGender = user.getGender();
		this.userAge = user.getAge();
		this.matchStatus = MatchStatus.IDLE;
	}

	public void update(UserMatchInfoUpdateRequestDto requestDto) {
		if (requestDto.getTitle() != null) {
			this.title = requestDto.getTitle();
		}
		if (requestDto.getAgeRange() != null) {
			this.ageRange = requestDto.getAgeRange();
		}
		if (requestDto.getMeetingDate() != null) {
			this.meetingDate = requestDto.getMeetingDate();
		}
		if (requestDto.getRegion() != null)
			this.region = requestDto.getRegion();
	}

	public void registerMatch() {
		this.matchStatus = MatchStatus.MATCHING;
		this.matchStartedAt = LocalDateTime.now();    // TODO: 매칭 되거나하면 null 로 변경
	}

	public boolean isMatching() {
		return !this.matchStatus.equals(MatchStatus.IDLE);
	}

	public boolean isStatus(MatchStatus status) {
		return this.matchStatus.equals(status);
	}

	public boolean isOwner(User user) {
		return this.getUser().equals(user);
	}

	public void updateStoreList(List<UserMatchInfoStore> storeList) {
		if (storeList != null) {
			this.storeList.clear();
			this.storeList.addAll(storeList);
		}
	}

	public void updateCategoryList(List<UserMatchInfoCategory> categoryListList) {
		if (categoryListList != null) {
			this.categoryList.clear();
			this.categoryList.addAll(categoryListList);
		}
	}

	public void updateMatchStatus(MatchStatus matchStatus) {
		this.matchStatus = matchStatus;
	}
}
