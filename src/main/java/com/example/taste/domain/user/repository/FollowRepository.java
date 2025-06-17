package com.example.taste.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.user.entity.Follow;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

	Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

	List<Follow> findByFollowerId(Long followerUId);

	List<Follow> findByFollowingId(Long followingId);

	boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
