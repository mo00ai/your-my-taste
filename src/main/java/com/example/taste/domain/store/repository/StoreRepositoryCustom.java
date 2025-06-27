package com.example.taste.domain.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.taste.domain.store.dto.response.StoreSearchResult;

public interface StoreRepositoryCustom {

	public Page<StoreSearchResult> searchByVector(float[] queryVector, double similarityThresh, Pageable pageable);

	// public Page<StoreSearchResult> searchByVector(float[] queryVector,
	// double similarityThresh,
	// Pageable pageable);

	// public Page<StoreSearchResult> searchByVectorWithFilters(StoreSearchCondition condition,
	// 	float[] queryVector,
	// 	Pageable pageable);

	// @Query(value = """
	// 	SELECT s.id, s.name, s.address, c.name as category_name,
	// 	       (1 - (s.embedding_vector <=> CAST(:vectorString AS vector))) as similarity
	// 	FROM store s
	// 	LEFT JOIN category c ON s.category_id = c.id
	// 	WHERE s.embedding_vector IS NOT NULL
	// 	AND (1 - (s.embedding_vector <=> CAST(:vectorString AS vector))) >= :threshold
	// 	ORDER BY similarity DESC
	// 	LIMIT :limit OFFSET :offset
	// 	""", nativeQuery = true)
	// List<Object[]> searchByVectorSimilarityNative(
	// 	@Param("vectorString") String vectorString,
	// 	@Param("threshold") double threshold,
	// 	@Param("limit") int limit,
	// 	@Param("offset") long offset);

}
