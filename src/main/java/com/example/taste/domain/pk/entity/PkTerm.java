package com.example.taste.domain.pk.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "pk_term")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PkTerm {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private int term;

	@Column(nullable = false)
	private LocalDateTime startDate;

	@Column(nullable = false)
	private LocalDateTime endDate;

	@Builder
	public PkTerm(int term, LocalDateTime startDate, LocalDateTime endDate) {
		this.term = term;
		this.startDate = startDate;
		this.endDate = endDate;
	}

}
