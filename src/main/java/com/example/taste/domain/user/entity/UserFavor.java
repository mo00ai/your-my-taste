package com.example.taste.domain.user.entity;

import jakarta.persistence.Entity;
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

import com.example.taste.domain.favor.entity.Favor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "user_favor")
public class UserFavor {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "favor_id", nullable = false)
	private Favor favor;

	@Builder
	public UserFavor(User user, Favor favor) {
		this.user = user;
		this.favor = favor;
	}
}
