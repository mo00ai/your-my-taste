package com.example.taste.domain.match.entity;

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

import com.example.taste.domain.store.entity.Store;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_match_info_store")
public class UserMatchInfoStore {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private UserMatchInfo userMatchInfo;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Store store;

	@Builder
	public UserMatchInfoStore(UserMatchInfo userMatchInfo, Store store) {
		this.userMatchInfo = userMatchInfo;
		this.store = store;
	}
}
