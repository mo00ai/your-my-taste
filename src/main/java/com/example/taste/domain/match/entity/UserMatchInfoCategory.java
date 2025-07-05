package com.example.taste.domain.match.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "user_match_info_category")
public class UserMatchInfoCategory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private UserMatchInfo userMatchInfo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Category category;

	@Builder
	public UserMatchInfoCategory(UserMatchInfo userMatchInfo, Category category) {
		this.userMatchInfo = userMatchInfo;
		this.category = category;
	}
}
