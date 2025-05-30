package com.example.taste.domain.pk.entity;

import com.example.taste.domain.pk.enums.PkType;

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

@Table(name = "pk_criteria")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PkCriteria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private PkType pkType;

	@Column(nullable = false)
	private int count;

	@Column(nullable = false)
	private boolean isActive;

	@Builder
	public PkCriteria(PkType pkType, Integer count, Boolean isActive) {
		this.pkType = pkType;
		this.count = count != null ? count : 0;
		this.isActive = isActive != null ? isActive : true;
	}

}
