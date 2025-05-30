package com.example.taste.domain.pk.entity;

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
public class PkTermRank {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private int rank;

	@Column(nullable = false)
	private int point;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pk_term_id", nullable = false)
	private PkTerm pkTerm;

	//user 연관관계

	@Builder
	public PkTermRank(int rank, Integer point, PkTerm pkTerm) {
		this.rank = rank;
		this.point = point != null ? point : 0;
		this.pkTerm = pkTerm;
	}

}
