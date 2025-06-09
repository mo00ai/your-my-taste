package com.example.taste.domain.match.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import com.example.taste.domain.match.dto.UserMatchCondCreateRequestDto;
import com.example.taste.domain.party.enums.MatchingStatus;
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

	private int ageMinRange;
	private int ageMaxRange;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	private String region;

	@Enumerated(EnumType.STRING)
	private MatchingStatus matchingStatus;

	private LocalDateTime matchStartedAt;

	@Builder
	public UserMatchCond(User user, List<UserMatchCondStore> stores, List<UserMatchCondCategory> categories,
		int ageMinRange,
		int ageMaxRange, Gender gender, String region, MatchingStatus matchingStatus) {
		this.user = user;
		this.stores = stores;
		this.categories = categories;
		this.ageMinRange = ageMinRange;
		this.ageMaxRange = ageMaxRange;
		this.gender = gender;
		this.region = region;
		this.matchingStatus = matchingStatus;
	}

	@Builder
	public UserMatchCond(UserMatchCondCreateRequestDto requestDto, User user) {
		this.user = user;
		this.ageMinRange = requestDto.getAgeMinRange();
		this.ageMaxRange = requestDto.getAgeMaxRange();
		this.gender = Gender.valueOf(requestDto.getGender());
		this.region = requestDto.getRegion();
		this.matchingStatus = MatchingStatus.IDLE;
	}
}
