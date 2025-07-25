package com.example.taste.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.user.dto.UserSigninProjectionDto;
import com.example.taste.domain.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom, UserRepositoryJooqCustom {
	Optional<User> findUserByEmail(String email);

	Optional<UserSigninProjectionDto> findSigninProjectionByEmail(String email);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.userFavorList WHERE u.id = :id")
	Optional<User> findByIdWithUserFavorList(@Param("id") Long id);

	boolean existsByEmail(String email);

	List<User> findAllByOrderByPointDesc(PageRequest of);

	List<User> findByPointGreaterThan(int i);

	@Query("SELECT DISTINCT f.following.id FROM Follow f WHERE f.follower.id = :userId")
	List<Long> findFollowingIds(@Param("userId") Long userId);

	Optional<User> findByIdAndDeletedAtIsNull(Long id);

	@Modifying
	@Query("UPDATE User u SET u.point = 0")
	void resetAllPoints();

}
