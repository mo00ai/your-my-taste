package com.example.taste.domain.pk.entity;

import com.example.taste.domain.pk.dto.request.PkUpdateRequestDto;
import com.example.taste.domain.pk.enums.PkType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PkType type;

	@Column(nullable = false)
	private int point;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Builder
	public PkCriteria(PkType type, Integer point, Boolean active) {
		this.type = type;
		this.point = point;
		this.active = active;
	}

	public void update(PkUpdateRequestDto dto) {
		this.type = dto.getType() != null ? PkType.valueOf(dto.getType()) : this.type;
		this.point = dto.getPoint() != null ? dto.getPoint() : this.point;
	}

	public void delete() {
		this.active = false;
	}

}
