package com.example.taste.domain.store.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import com.example.taste.common.entity.SoftDeletableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store", uniqueConstraints = {    // 이름(name), 경도(mapx), 위도(mapy)를 묶어서 유니크 제약
	@UniqueConstraint(columnNames = {"name", "mapx", "mapy"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Store extends SoftDeletableEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@Column(nullable = false)
	private String name;

	private String description;

	private String address;

	private String roadAddress;

	@Column(precision = 10, scale = 7, nullable = false)
	private BigDecimal mapx;

	@Column(precision = 10, scale = 7, nullable = false)
	private BigDecimal mapy;

	@Builder
	public Store(Category category, String name, String description, String address, String roadAddress,
		BigDecimal mapx, BigDecimal mapy) {

		this.category = category;
		this.name = name;
		this.description = description;
		this.address = address;
		this.roadAddress = roadAddress;
		this.mapx = mapx;
		this.mapy = mapy;
	}

	public static Map<BigDecimal, BigDecimal> switchCoordinates(String lng, String lat) {
		return Map.of(new BigDecimal(lng).setScale(7, RoundingMode.HALF_UP),
			new BigDecimal(lat).setScale(7, RoundingMode.HALF_UP));
	}
}
