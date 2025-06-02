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

import com.example.taste.domain.store.entity.Store;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_match_cond_store")
public class UserMatchCondStore {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private UserMatchCond userMatchCond;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Store store;

	@Builder
	public UserMatchCondStore(UserMatchCond userMatchCond, Store store) {
		this.userMatchCond = userMatchCond;
		this.store = store;
	}
}
