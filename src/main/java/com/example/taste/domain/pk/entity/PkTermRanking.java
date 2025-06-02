package com.example.taste.domain.pk.entity;

import com.example.taste.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "pk_term_rank")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PkTermRanking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private int ranking;

	@Column(nullable = false)
	private int point;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pk_term_id", nullable = false)
	private PkTerm pkTerm;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Builder
	public PkTermRanking(int ranking, Integer point, PkTerm pkTerm, User user) {
		this.ranking = ranking;
		this.point = point != null ? point : 0;
		this.pkTerm = pkTerm;
		this.user = user;
	}

}
