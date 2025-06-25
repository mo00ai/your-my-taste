package com.example.taste.domain.store.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.user.entity.User;

public interface StoreBucketRepository extends JpaRepository<StoreBucket, Long>, StoreBucketRepositoryCustom {
	List<StoreBucket> findByUserAndNameStartingWith(User user, String name);

	// @EntityGraph(attributePaths = "user")
	// Page<StoreBucket> findAllByUserAndIsOpened(User targetUser, boolean isOpened,
	// 	Pageable pageable);

	void deleteAllByUser(User user);

	@EntityGraph(attributePaths = "user")
	List<StoreBucket> findAllByIdIn(Collection<Long> ids);
}
