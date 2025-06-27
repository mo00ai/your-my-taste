package com.example.taste.domain.store.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
import lombok.Setter;

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
	// 행정동 주소
	private String address;

	private String roadAddress;

	// 행정동 지역 정보
	// 시/도 정보
	@Column(name = "sido", length = 50)
	private String sido;
	// 시/군/구 정보
	@Column(name = "sigungu", length = 50)
	private String sigungu;
	// 읍/면/동 정보
	@Column(name = "eupmyeondong", length = 50)
	private String eupmyeondong;
	// 경도
	@Column(precision = 10, scale = 7, nullable = false)
	private BigDecimal mapx;
	// 위도
	@Column(precision = 10, scale = 7, nullable = false)
	private BigDecimal mapy;

	// // 임베딩 벡터 (pgvector)
	// @Column(name = "embedding_vector", columnDefinition = "vector(1536)", nullable = false)
	// private PGvector embeddingVector;

	// Critical: Use these specific annotations
	@Setter
	@Column(name = "embedding_vector")
	@JdbcTypeCode(SqlTypes.VECTOR)
	@Array(length = 1536) // Must match your embedding dimensions exactly
	private float[] embeddingVector;

	@Builder
	public Store(Category category, String name, String description, String address, String roadAddress,
		String sido, String sigungu, String eupmyeondong,
		BigDecimal mapx, BigDecimal mapy) {

		this.category = category;
		this.name = name;
		this.description = description;
		this.address = address;
		this.roadAddress = roadAddress;
		this.sido = sido;
		this.sigungu = sigungu;
		this.eupmyeondong = eupmyeondong;
		this.mapx = mapx;
		this.mapy = mapy;
	}

	public void setCoordinates(String lng, String lat) {
		this.mapx = new BigDecimal(lng).divide(new BigDecimal("10000000"), 7,
			RoundingMode.HALF_UP);
		this.mapy = new BigDecimal(lat).divide(new BigDecimal("10000000"), 7,
			RoundingMode.HALF_UP);
	}
}
