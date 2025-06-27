package com.example.taste.domain.store.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.taste.domain.store.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {
	// 중복 체크용 메서드
	boolean existsByNameAndMapxAndMapy(String storeName, BigDecimal longitude, BigDecimal latitude);

	List<Store> findByEmbeddingVectorIsNull();

	Page<Store> findByEmbeddingVectorIsNull(Pageable pageable);

	@Query("""
		    SELECT s FROM Store s
		    JOIN FETCH s.category c
		    WHERE s.embeddingVector IS NULL
		""")
	Page<Store> findByEmbeddingVectorIsNullWithCategory(Pageable pageable);
}
