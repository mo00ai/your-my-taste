package com.example.taste.domain.embedding.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.example.taste.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_search", schema = "search" )
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreSearch extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// MySQL 연동용 primaryKey
	@Column(name = "mysql_store_id", unique = true, nullable = false)
	private Long mysqlStoreId;

	// 검색용 최소 정보(가게명)
	@Column(name = "store_name", nullable = false, length = 255)
	private String name;

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

	// 카테고리 이름으로 저장
	@Column(name = "category_name", nullable = false, length = 100)
	private String categoryName;

	// 위도
	@Column(precision = 10, scale = 7, nullable = false)
	private BigDecimal latitude;
	// 경도
	@Column(precision = 11, scale = 7, nullable = false)
	private BigDecimal longitude;

	// 임베딩 벡터 (pgvector)
	@Column(name = "embedding_vector", columnDefinition = "vector(1536)", nullable = false)
	private String embeddingVector;

	@Enumerated(EnumType.STRING)
	private SyncStatus syncStatus = SyncStatus.SYNCED;

	private LocalDateTime lastSyncedAt;

	@Builder
	public StoreSearch(Long mysqlStoreId, String name, String sido, String sigungu, String eupmyeondong,
		String categoryName, String latitude, String longitude, String embeddingVector) {
		this.mysqlStoreId = mysqlStoreId;
		this.name = name;
		this.sido = sido;
		this.sigungu = sigungu;
		this.eupmyeondong = eupmyeondong;
		this.categoryName = categoryName;
		setCoordinates(latitude, longitude);
		this.embeddingVector = embeddingVector;
	}

	public void setCoordinates(String lat, String lng) {
		this.latitude = new BigDecimal(lat).setScale(7, RoundingMode.HALF_UP);
		this.longitude = new BigDecimal(lng).setScale(7, RoundingMode.HALF_UP);
	}

	public void markPending() {
		this.syncStatus = SyncStatus.PENDING;
	}

	public void markSyncFailed() {
		this.syncStatus = SyncStatus.FAILED;
	}

	public void markSynced() {
		this.syncStatus = SyncStatus.SYNCED;
		this.lastSyncedAt = LocalDateTime.now();
	}

	public enum SyncStatus {
		SYNCED, PENDING, FAILED
	}
}
