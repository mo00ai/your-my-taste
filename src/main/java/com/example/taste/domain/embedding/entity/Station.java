package com.example.taste.domain.embedding.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "station", uniqueConstraints = {    // 역 명(name), 호선(line)을 묶어서 유니크 제약
	@UniqueConstraint(
		name = "uk_station_name_line",
		columnNames = {"name", "line"}
	)
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Station {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	// 역명
	@Column(nullable = false)
	private String name;
	// 호선명
	@Column(nullable = false)
	private String line;
	// 경도
	@Column(precision = 10, scale = 7, nullable = false)
	private BigDecimal latitude;
	// 위도
	@Column(precision = 10, scale = 7, nullable = false)
	private BigDecimal longitude;

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

	// 임베딩 벡터 (pgvector)
	@Column(name = "embedding_vector")
	@JdbcTypeCode(SqlTypes.VECTOR)
	@Array(length = 1536) // Must match your embedding dimensions exactly
	private float[] embeddingVector;

	@Builder
	public Station(String name, String line, String latitude, String longitude, String sido, String sigungu,
		String eupmyeondong) {
		this.name = name;
		this.line = line;
		setCoordinates(longitude, latitude);
		this.sido = sido;
		this.sigungu = sigungu;
		this.eupmyeondong = eupmyeondong;
	}

	public void setCoordinates(String lng, String lat) {
		this.longitude = new BigDecimal(lng).setScale(7, RoundingMode.HALF_UP);
		this.latitude = new BigDecimal(lat).setScale(7, RoundingMode.HALF_UP);

	}

	public void setEmbeddingVector(float[] embeddingVector) {
		this.embeddingVector = embeddingVector;
	}

	@Override
	public String toString() {
		return "Station{" +
			"id=" + id +
			", name='" + name + '\'' +
			", line='" + line + '\'' +
			", sido='" + sido + '\'' +
			", sigungu='" + sigungu + '\'' +
			", eupmyeondong='" + eupmyeondong + '\'' +
			", latitude=" + latitude +
			", longitude=" + longitude +
			"}";
	}

}
