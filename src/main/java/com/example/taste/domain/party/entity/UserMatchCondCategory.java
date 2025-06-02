package com.example.taste.domain.party.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.example.taste.domain.store.entity.Category;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_match_cond_category")
public class UserMatchCondCategory {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private UserMatchCond userMatchCond;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Category category;

	@Builder
	public UserMatchCondCategory(UserMatchCond userMatchCond, Category category) {
		this.userMatchCond = userMatchCond;
		this.category = category;
	}
}
