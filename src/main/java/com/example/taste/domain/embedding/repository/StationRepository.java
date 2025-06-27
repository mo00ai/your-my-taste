package com.example.taste.domain.embedding.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.embedding.entity.Station;

public interface StationRepository extends JpaRepository<Station, Long> {
	// @Modifying
	// @Transactional
	// @Query(
	// 	value = "INSERT INTO station (name, line, latitude, longitude, sido, sigungu, eupmyeondong, embedding_vector) "
	// 		+
	// 		"VALUES (:name, :line, :lat, :lng, :sido, :sigungu, :eupmyeondong, :vector)",
	// 	nativeQuery = true
	// )
	// int bulkInsertSingle(
	// 	@Param("name") String name,
	// 	@Param("line") String line,
	// 	@Param("lat") String latitude,
	// 	@Param("lng") String longitude,
	// 	@Param("sido") String sido,
	// 	@Param("sigungu") String sigungu,
	// 	@Param("eupmyeondong") String eupmyeondong,
	// 	@Param("vector") String embeddingVector
	// );
}
