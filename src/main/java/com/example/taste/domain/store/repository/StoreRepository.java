package com.example.taste.domain.store.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.store.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {
	// 중복 체크용 메서드
	boolean existsByNameAndMapxAndMapy(String storeName, BigDecimal longitude, BigDecimal latitude);
}
