package com.example.taste.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
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

	// notification 용 전체 유저 id 검색 메서드
	@Query("select u.id from User u")
	List<Long> findAllUserId();
}
