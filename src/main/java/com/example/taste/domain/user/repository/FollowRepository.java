package com.example.taste.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.user.entity.Follow;
import com.example.taste.domain.user.entity.User;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

	@Query("SELECT f FROM Follow f "
		+ "WHERE f.follower.id = :followerUserId AND f.following.id = :followingUserId")
	Follow findByFollowerAndFollower(
		@Param("followerUserId") Long followerUserId, @Param("followingUserId") Long followingUserId);

	@Query("SELECT f FROM Follow f JOIN FETCH f.following "
		+ "WHERE f.follower.id = :followerUserId")
	List<Follow> findAllByFollower(@Param("followerUserId") Long followerUserId);

	@Query("SELECT f FROM Follow f JOIN FETCH f.follower "
		+ "WHERE f.following.id = :followingUserId")
	List<Follow> findAllByFollowing(@Param("followingUserId") Long followingUserId);

	Optional<Follow> findByFollowerAndFollowing(User follower, User following);
}
