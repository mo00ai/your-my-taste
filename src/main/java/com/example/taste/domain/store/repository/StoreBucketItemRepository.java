package com.example.taste.domain.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.store.entity.StoreBucketItem;

public interface StoreBucketItemRepository extends JpaRepository<StoreBucketItem, Long> {
}
