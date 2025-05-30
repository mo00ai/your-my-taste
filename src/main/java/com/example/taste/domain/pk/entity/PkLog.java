package com.example.taste.domain.pk.entity;

import com.example.taste.common.entity.BaseCreatedAtEntity;
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

@Table(name = "pk_log")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PkLog extends BaseCreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private PkType pkType;

	@Column(nullable = false)
	private int point = 0;

	//user와 연관관계

	@Builder
	public PkLog(PkType pkType, Integer point) {
		this.pkType = pkType;
		this.point = point != null ? point : 0;
	}

}
