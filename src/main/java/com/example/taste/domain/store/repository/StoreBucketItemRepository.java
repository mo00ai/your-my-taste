package com.example.taste.domain.store.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.entity.StoreBucketItem;

public interface StoreBucketItemRepository
	extends JpaRepository<StoreBucketItem, Long>, StoreBucketItemRepositoryCustom {
	// @EntityGraph(attributePaths = "store")
	// Page<StoreBucketItem> findAllByStoreBucket(StoreBucket storeBucket, Pageable pageable);

	void deleteAllByStoreBucket(StoreBucket storeBucket);

	boolean existsByStoreBucketAndStore(StoreBucket storeBucket, Store store);

	void deleteAllByStore(Store store);

	@EntityGraph(attributePaths = "storeBucket")
	List<StoreBucketItem> findAllByIdIn(List<Long> ids);
}
