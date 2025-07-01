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

import com.example.taste.domain.favor.entity.Favor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "party_match_info_favor")
public class PartyMatchInfoFavor {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private PartyMatchInfo partyMatchInfo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Favor favor;

	@Builder
	public PartyMatchInfoFavor(PartyMatchInfo partyMatchInfo, Favor favor) {
		this.partyMatchInfo = partyMatchInfo;
		this.favor = favor;
	}
}
