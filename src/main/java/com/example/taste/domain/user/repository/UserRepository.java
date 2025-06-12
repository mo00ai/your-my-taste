package com.example.taste.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findUserByEmail(String email);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.userFavorList WHERE u.id = :id")
	Optional<User> findByIdWithUserFavorList(@Param("id") Long id);

	boolean existsByEmail(String email);

	// 활성화된 유저만 가져오기
	List<User> findAllByDeletedAtIsNull();

	List<User> findAllByOrderByPointDesc(PageRequest of);

	List<User> findByPointGreaterThan(int i);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE User u SET u.postingCount = 0")
	void resetPostingCnt();

	@Modifying(clearAutomatically = true)
	@Query("UPDATE User u SET u.postingCount = u.postingCount + 1 WHERE u.id = :userId AND u.postingCount < :limit")
	int increasePostingCount(@Param("userId") Long userId, @Param("limit") int limit);

	@Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId")
	List<Long> findFollowingIds(@Param("userId") Long userId);
}
