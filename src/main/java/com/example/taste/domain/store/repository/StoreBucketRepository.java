package com.example.taste.domain.store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.store.entity.StoreBucket;

public interface StoreBucketRepository extends JpaRepository<StoreBucket, Long> {
	Optional<StoreBucket> findById(Long id);
}
