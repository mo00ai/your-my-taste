package com.example.taste.domain.match.entity;

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
import com.example.taste.domain.match.dto.request.UserMatchCondCreateRequestDto;
import com.example.taste.domain.match.dto.request.UserMatchCondUpdateRequestDto;
import com.example.taste.domain.match.vo.AgeRange;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.enums.Gender;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_match_cond")
public class UserMatchCond extends BaseCreatedAtEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Setter
	@OneToMany(mappedBy = "userMatchCond", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserMatchCondStore> stores;

	@Setter
	@OneToMany(mappedBy = "userMatchCond", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserMatchCondCategory> categories;

	@Embedded
	private AgeRange ageRange;
	private LocalDateTime meetingTime;
	private String region;

	@Enumerated(EnumType.STRING)
	private Gender userGender;
	private Integer userAge;

	@Setter
	@Enumerated(EnumType.STRING)
	private MatchStatus matchStatus;

	private LocalDateTime matchStartedAt;

	@Builder
	public UserMatchCond(User user, List<UserMatchCondStore> stores, List<UserMatchCondCategory> categories,
		AgeRange ageRange, Gender userGender, Integer userAge, String region, MatchStatus matchStatus) {
		this.user = user;
		this.stores = stores;
		this.categories = categories;
		this.ageRange = ageRange;
		this.region = region;
		this.userGender = userGender;
		this.userAge = userAge;
		this.matchStatus = matchStatus;
	}

	@Builder
	public UserMatchCond(UserMatchCondCreateRequestDto requestDto, User user) {
		this.user = user;
		this.ageRange = requestDto.getAgeRange();
		this.region = requestDto.getRegion();
		this.userGender = user.getGender();
		this.userAge = user.getAge();
		this.matchStatus = MatchStatus.IDLE;
	}

	public void update(UserMatchCondUpdateRequestDto requestDto) {
		this.ageRange = requestDto.getAgeRange();
		this.userGender = Gender.valueOf(requestDto.getGender());
		this.region = requestDto.getRegion();
	}

	public void registerMatch() {
		this.matchStatus = MatchStatus.MATCHING;
		this.matchStartedAt = LocalDateTime.now();
	}
}
