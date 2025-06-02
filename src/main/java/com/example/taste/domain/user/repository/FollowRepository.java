package com.example.taste.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.user.entity.Follow;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

	@Query("SELECT f FROM Follow f "
		+ "WHERE f.follower.id = :followerUserId AND f.following.id = :followingUserId")
	Follow findByFollowerAndFollower(
		@Param("followerUserId") Long followerUserId, @Param("followingUserId") Long followingUserId);
}
