package com.example.taste.domain.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.entity.StoreBucketItem;

public interface StoreBucketItemRepository extends JpaRepository<StoreBucketItem, Long> {
	List<StoreBucketItem> findAllByStoreBucket(StoreBucket storeBucket);

	StoreBucket store(Store store);

	Optional<StoreBucketItem> findByStoreAndStoreBucket(Store store, StoreBucket storeBucket);
}
