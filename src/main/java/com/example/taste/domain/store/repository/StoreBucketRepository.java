package com.example.taste.domain.store.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.user.entity.User;

public interface StoreBucketRepository extends JpaRepository<StoreBucket, Long>, StoreBucketRepositoryCustom {
	List<StoreBucket> findByUserAndNameStartingWith(User user, String name);

	List<StoreBucket> findAllByUserAndIsOpened(User targetUser, boolean b);
}
