package com.example.taste.domain.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.store.entity.StoreBucket;

public interface StoreBucketRepository extends JpaRepository<StoreBucket, Long> {
}
