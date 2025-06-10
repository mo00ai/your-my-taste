package com.example.taste.domain.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.user.entity.UserFavor;

@Repository
public interface UserFavorRepository extends JpaRepository<UserFavor, Long> {
	@Query("SELECT DISTINCT uf FROM UserFavor uf JOIN FETCH uf.user WHERE uf.user.id = :userId")
	List<UserFavor> findAllByUser(@Param("userId") Long userId);
}
