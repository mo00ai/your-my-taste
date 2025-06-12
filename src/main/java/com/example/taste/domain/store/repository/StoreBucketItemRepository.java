package com.example.taste.domain.store.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.entity.StoreBucketItem;

public interface StoreBucketItemRepository extends JpaRepository<StoreBucketItem, Long> {
	Page<StoreBucketItem> findAllByStoreBucket(StoreBucket storeBucket, Pageable pageable);

	StoreBucket store(Store store);

	Optional<StoreBucketItem> findByStoreAndStoreBucket(Store store, StoreBucket storeBucket);

	void deleteAllByStoreBucket(StoreBucket storeBucket);

	boolean existsByStoreBucketAndStore(StoreBucket storeBucket, Store store);
}
