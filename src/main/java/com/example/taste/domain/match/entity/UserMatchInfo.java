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
import lombok.Setter;

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
@Table(name = "user_match_info")        // MEMO : 유저 매칭 정보로 변경할까? 헷갈림
public class UserMatchInfo extends BaseCreatedAtEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Setter
	@OneToMany(mappedBy = "userMatchInfo", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserMatchInfoStore> stores;

	@Setter
	@OneToMany(mappedBy = "userMatchInfo", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserMatchInfoCategory> categories;

	@Embedded
	private AgeRange ageRange;
	private LocalDate meetingDate;
	private String region;

	@Enumerated(EnumType.STRING)
	private Gender userGender;
	private Integer userAge;

	@Setter
	@Enumerated(EnumType.STRING)
	private MatchStatus matchStatus;

	private LocalDateTime matchStartedAt;

	@Builder
	public UserMatchInfo(User user, List<UserMatchInfoStore> stores, List<UserMatchInfoCategory> categories,
		AgeRange ageRange, LocalDate meetingDate, Gender userGender, Integer userAge, String region,
		MatchStatus matchStatus) {
		this.user = user;
		this.stores = stores;
		this.categories = categories;
		this.ageRange = ageRange;
		this.meetingDate = meetingDate;
		this.region = region;
		this.userGender = userGender;
		this.userAge = userAge;
		this.matchStatus = matchStatus;
	}

	@Builder
	public UserMatchInfo(UserMatchInfoCreateRequestDto requestDto, User user) {
		this.user = user;
		this.ageRange = requestDto.getAgeRange();
		this.region = requestDto.getRegion();
		this.userGender = user.getGender();
		this.userAge = user.getAge();
		this.matchStatus = MatchStatus.IDLE;
	}

	public void update(UserMatchInfoUpdateRequestDto requestDto) {
		this.ageRange = requestDto.getAgeRange();
		this.region = requestDto.getRegion();
	}

	public void registerMatch() {
		this.matchStatus = MatchStatus.MATCHING;
		this.matchStartedAt = LocalDateTime.now();
	}

	public boolean isMatching() {
		return !this.matchStatus.equals(MatchStatus.IDLE);
	}

	public boolean isStatus(MatchStatus status) {
		return this.matchStatus.equals(status);
	}
}
