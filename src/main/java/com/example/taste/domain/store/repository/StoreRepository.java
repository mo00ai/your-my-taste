package com.example.taste.domain.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.taste.domain.store.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM Store s WHERE s.id = :id")
	int deleteByIdReturningCount(@Param("id") Long id);
}
