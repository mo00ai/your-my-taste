package com.example.taste.domain.store.entity;

import java.math.BigDecimal;

import com.example.taste.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store")
@NoArgsConstructor
@AllArgsConstructor
public class Store extends BaseEntity {
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

	@Column(nullable = false)
	private BigDecimal mapx;

	@Column(nullable = false)
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
}
