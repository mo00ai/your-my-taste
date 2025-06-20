package com.example.taste.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
	Optional<User> findUserByEmail(String email);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.userFavorList WHERE u.id = :id")
	Optional<User> findByIdWithUserFavorList(@Param("id") Long id);

	boolean existsByEmail(String email);

	List<User> findAllByOrderByPointDesc(PageRequest of);

	List<User> findByPointGreaterThan(int i);

	// notification 용 전체 유저 id 검색 메서드
	@Query("select u.id from User u where u.deletedAt is null")
	List<Long> findAllUserId();

	// 리팩토링 전 코드 - 성능 비교때 사용할 예정
	// @Modifying(clearAutomatically = true)
	// @Query("UPDATE User u SET u.postingCount = 0")
	// long resetPostingCnt();
	// @Modifying(clearAutomatically = true)
	// @Query("UPDATE User u SET u.postingCount = u.postingCount + 1 WHERE u.id = :userId AND u.postingCount < :limit")
	// int increasePostingCount(@Param("userId") Long userId, @Param("limit") int limit);

	@Query("SELECT DISTINCT f.following.id FROM Follow f WHERE f.follower.id = :userId")
	List<Long> findFollowingIds(@Param("userId") Long userId);

	Optional<User> findByIdAndDeletedAtIsNull(Long id);

	@Query("SELECT u FROM User u JOIN FETCH u.image WHERE u.id = :id")
	Optional<User> findByIdWithImage(@Param("id") Long id);

}
