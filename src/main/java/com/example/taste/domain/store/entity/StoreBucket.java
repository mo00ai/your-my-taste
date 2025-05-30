package com.example.taste.domain.store.entity;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_bucket")
@NoArgsConstructor
public class StoreBucket {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private boolean isOpened;

	@Builder
	public StoreBucket(String name, Boolean isOpened) {
		this.name = StringUtils.isBlank(name) ? "기본 리스트" : name;
		this.isOpened = isOpened == null || isOpened;
	}
}
