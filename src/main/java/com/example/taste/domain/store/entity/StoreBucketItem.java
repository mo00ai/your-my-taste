package com.example.taste.domain.store.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bucket_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class StoreBucketItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OnDelete(action = OnDeleteAction.CASCADE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bucket_id", nullable = false)
	private StoreBucket storeBucket;

	@OnDelete(action = OnDeleteAction.CASCADE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Builder
	public StoreBucketItem(StoreBucket storeBucket, Store store) {
		this.storeBucket = storeBucket;
		this.store = store;
	}
}
