package com.example.taste.domain.store.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.store.dto.response.StoreSearchResult;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class StoreRepositoryImpl implements StoreRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * float 배열을 PostgreSQL vector 형식 문자열로 변환
	 */
	private String formatVector(float[] vector) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < vector.length; i++) {
			if (i > 0)
				sb.append(",");
			sb.append(vector[i]);
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Page<StoreSearchResult> searchByVector(float[] queryVector, double similarityThresh, Pageable pageable) {

		String vectorString = formatVector(queryVector);

		// 메인 검색 쿼리
		String sql = """
			SELECT
			    s.id,
			    s.name,
			    s.address,
			    c.name as category_name,
			    (1 - (s.embedding_vector <=> CAST(:vectorString AS vector))) as similarity
			FROM store s
			LEFT JOIN category c ON s.category_id = c.id
			WHERE s.embedding_vector IS NOT NULL
			AND s.deleted_at IS NULL
			AND (1 - (s.embedding_vector <=> CAST(:vectorString AS vector))) >= :threshold
			ORDER BY similarity DESC
			LIMIT :limit OFFSET :offset
			""";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("vectorString", vectorString);
		query.setParameter("threshold", similarityThresh);
		query.setParameter("limit", pageable.getPageSize());
		query.setParameter("offset", pageable.getOffset());

		List<Object[]> rawResults = query.getResultList();

		List<StoreSearchResult> content = rawResults.stream()
			.map(row -> {
				// Null 체크 추가
				Long id = row[0] != null ? ((Number)row[0]).longValue() : null;
				String name = (String)row[1];
				String address = (String)row[2];
				String categoryName = (String)row[3];
				Double similarity = row[4] != null ? ((Number)row[4]).doubleValue() : 0.0;

				return new StoreSearchResult(id, name, address, categoryName, similarity);
			})
			.collect(Collectors.toList());

		// Count 쿼리
		String countSql = """
			SELECT COUNT(*)
			FROM store s
			WHERE s.embedding_vector IS NOT NULL
			AND s.deleted_at IS NULL
			AND (1 - (s.embedding_vector <=> CAST(:vectorString AS vector))) >= :threshold
			""";

		Query countQuery = entityManager.createNativeQuery(countSql);
		countQuery.setParameter("vectorString", vectorString);
		countQuery.setParameter("threshold", similarityThresh);

		Long total = ((Number)countQuery.getSingleResult()).longValue();

		log.debug("Vector search completed: {} results, similarity threshold: {}", content.size(), similarityThresh);

		return new PageImpl<>(content, pageable, total);
	}

	// TODO 구현 수정 필요
	// @Override
	// public Page<StoreSearchResult> searchByVectorWithFilters(StoreSearchCondition condition, float[] queryVector,
	// 	Pageable pageable) {
	//
	// 	String vectorString = formatVector(queryVector);
	// 	double threshold = condition.getSimilarityThreshold() != null ? condition.getSimilarityThreshold() : 0.7;
	//
	// 	// 동적 SQL 구성
	// 	StringBuilder sqlBuilder = new StringBuilder();
	// 	StringBuilder countSqlBuilder = new StringBuilder();
	// 	List<String> parameters = new ArrayList<>();
	//
	// 	// SELECT 절
	// 	sqlBuilder.append("""
	// 		SELECT
	// 		    s.id,
	// 		    s.name,
	// 		    s.address,
	// 		    c.name as category_name,
	// 		    (1 - (s.embedding_vector <=> CAST(? AS vector))) as similarity
	// 		""");
	// 	parameters.add(vectorString);
	// 	double lat = 0d, lon = 0d;
	// 	// 지리적 거리도 포함할지 결정
	// 	if (condition.hasGeo()) {
	// 		sqlBuilder.append(", haversine_distance(s.mapy, s.mapx, ?, ?) as distance");
	// 		lat = Math.round(Double.parseDouble(condition.getLatitude()) * 1e7) / 1e7;
	// 		lon = Math.round(Double.parseDouble(condition.getLongitude()) * 1e7) / 1e7;
	// 		parameters.add(String.valueOf(lat));
	// 		parameters.add(String.valueOf(lon));
	// 	}
	//
	// 	// FROM 절
	// 	sqlBuilder.append("""
	// 		FROM store s
	// 		LEFT JOIN category c ON s.category_id = c.id
	// 		WHERE s.embedding_vector IS NOT NULL
	// 		AND s.deleted_at IS NULL
	// 		AND (1 - (s.embedding_vector <=> CAST(? AS vector))) >= ?
	// 		""");
	// 	parameters.add(vectorString);
	// 	parameters.add(String.valueOf(threshold));
	//
	// 	// 카테고리 필터
	// 	if (condition.getCategory() != null && !condition.getCategory().isBlank()) {
	// 		sqlBuilder.append(" AND LOWER(c.name) = LOWER(?)");
	// 		parameters.add(condition.getCategory());
	// 	}
	//
	// 	// 지리적 필터
	// 	if (condition.hasGeo()) {
	// 		int radius = condition.getRadius() != null ? condition.getRadius() : 3000;
	// 		lat = Math.round(Double.parseDouble(condition.getLatitude()) * 1e7) / 1e7;
	// 		lon = Math.round(Double.parseDouble(condition.getLongitude()) * 1e7) / 1e7;
	//
	// 		sqlBuilder.append(" AND haversine_distance(s.mapy, s.mapx, ?, ?) < ?");
	// 		parameters.add(String.valueOf(lat));
	// 		parameters.add(String.valueOf(lon));
	// 		parameters.add(String.valueOf(radius));
	// 	}
	//
	// 	// ORDER BY 절
	// 	sqlBuilder.append(" ORDER BY similarity DESC");
	// 	if (condition.hasGeo()) {
	// 		sqlBuilder.append(", distance ASC");
	// 	}
	//
	// 	// LIMIT/OFFSET 절
	// 	sqlBuilder.append(" LIMIT ? OFFSET ?");
	// 	parameters.add(String.valueOf(pageable.getPageSize()));
	// 	parameters.add(String.valueOf(pageable.getOffset()));
	//
	// 	// 메인 쿼리 실행
	// 	Query query = entityManager.createNativeQuery(sqlBuilder.toString());
	// 	for (int i = 0; i < parameters.size(); i++) {
	// 		query.setParameter(i + 1, parameters.get(i));
	// 	}
	//
	// 	@SuppressWarnings("unchecked")
	// 	List<Object[]> rawResults = query.getResultList();
	//
	// 	List<StoreSearchResult> content = rawResults.stream()
	// 		.map(row -> new StoreSearchResult(
	// 			((Number)row[0]).longValue(),
	// 			(String)row[1],
	// 			(String)row[2],
	// 			(String)row[3],
	// 			((Number)row[4]).doubleValue()
	// 		))
	// 		.collect(Collectors.toList());
	//
	// 	// Count 쿼리 구성 (동일한 WHERE 조건 사용)
	// 	countSqlBuilder.append("""
	// 		SELECT COUNT(*)
	// 		FROM store s
	// 		LEFT JOIN category c ON s.category_id = c.id
	// 		WHERE s.embedding_vector IS NOT NULL
	// 		AND s.deleted_at IS NULL
	// 		AND (1 - (s.embedding_vector <=> CAST(? AS vector))) >= ?
	// 		""");
	//
	// 	List<String> countParameters = new ArrayList<>();
	// 	countParameters.add(vectorString);
	// 	countParameters.add(String.valueOf(threshold));
	//
	// 	if (condition.getCategory() != null && !condition.getCategory().isBlank()) {
	// 		countSqlBuilder.append(" AND LOWER(c.name) = LOWER(?)");
	// 		countParameters.add(condition.getCategory());
	// 	}
	//
	// 	if (condition.hasGeo()) {
	// 		int radius = condition.getRadius() != null ? condition.getRadius() : 3000;
	// 		lat = Math.round(Double.parseDouble(condition.getLatitude()) * 1e7) / 1e7;
	// 		lon = Math.round(Double.parseDouble(condition.getLongitude()) * 1e7) / 1e7;
	//
	// 		countSqlBuilder.append(" AND haversine_distance(s.mapy, s.mapx, ?, ?) < ?");
	// 		countParameters.add(String.valueOf(lat));
	// 		countParameters.add(String.valueOf(lon));
	// 		countParameters.add(String.valueOf(radius));
	// 	}
	//
	// 	Query countQuery = entityManager.createNativeQuery(countSqlBuilder.toString());
	// 	for (int i = 0; i < countParameters.size(); i++) {
	// 		countQuery.setParameter(i + 1, countParameters.get(i));
	// 	}
	//
	// 	Long total = ((Number)countQuery.getSingleResult()).longValue();
	//
	// 	log.debug("Filtered vector search completed: {} results, filters: category={}, geo={}",
	// 		content.size(), condition.getCategory(), condition.hasGeo());
	//
	// 	return new PageImpl<>(content, pageable, total);
	// }

	// ####################################################################################
	// @Override
	// public Page<StoreSearchResult> searchByVector(float[] queryVector, double similarityThresh, Pageable pageable) {
	// 	// CosineSimilarity 식 생성 (SELECT 절, WHERE 절, ORDER BY 절에서 재사용)
	// 	NumberExpression<Double> similarity = VectorQueryDslExtension.cosineSimilarity(
	// 		store.embeddingVector, queryVector);
	//
	// 	// 메인 데이터 조회 (WITH JOIN category)
	// 	List<StoreSearchResult> content = queryFactory
	// 		.select(Projections.constructor(StoreSearchResult.class,
	// 			store.id,
	// 			store.name,
	// 			store.address,
	// 			category.name,
	// 			similarity              // projection 에 계산 컬럼 포함
	// 		))
	// 		.from(store)
	// 		.leftJoin(store.category, category)
	// 		.where(store.embeddingVector.isNotNull(),
	// 			similarity.goe(similarityThresh))
	// 		.orderBy(similarity.desc())
	// 		.offset(pageable.getOffset())
	// 		.limit(pageable.getPageSize())
	// 		.fetch();
	//
	// 	Long total = queryFactory
	// 		.select(store.count())
	// 		.from(store)
	// 		.where(store.embeddingVector.isNotNull(),
	// 			VectorQueryDslExtension.cosineSimilarity(store.embeddingVector, queryVector)
	// 				.goe(similarityThresh))
	// 		.fetchOne();
	// 	return new PageImpl<>(content, pageable, total != null ? total : 0L);
	// }
	//
	// @Override
	// public Page<StoreSearchResult> searchByVectorWithFilters(StoreSearchCondition condition, float[] queryVector,
	// 	Pageable pageable) {
	// 	NumberExpression<Double> similarity = VectorQueryDslExtension.cosineSimilarity(
	// 		store.embeddingVector, queryVector);
	//
	// 	BooleanBuilder whereCondition = new BooleanBuilder()
	// 		.and(store.embeddingVector.isNotNull())      // 임베딩 존재 여부 필수
	// 		.and(similarity.goe(condition.getSimilarityThreshold() != null ?
	// 			condition.getSimilarityThreshold() : 0.7));
	// 	double lat = Math.round(Double.parseDouble(condition.getLatitude()) * 1e7) / 1e7;
	// 	double lon = Math.round(Double.parseDouble(condition.getLongitude()) * 1e7) / 1e7;
	// 	// 카테고리 이름 필터
	// 	if (condition.getCategory() != null && !condition.getCategory().isBlank()) {
	// 		whereCondition.and(store.category.name.equalsIgnoreCase(condition.getCategory()));
	// 	}
	// 	if (condition.hasGeo()) {
	// 		int radius = condition.getRadius() != null ? condition.getRadius() : 3000;
	// 		whereCondition.and(VectorQueryDslExtension.withinRadius(store.mapy, store.mapx, lat, lon, radius));
	// 	}
	// 	NumberExpression<Double> distance = VectorQueryDslExtension.haversineDistance(
	// 		store.mapy, store.mapx, lat, lon
	// 	);
	//
	// 	List<StoreSearchResult> content = queryFactory
	// 		.select(Projections.constructor(StoreSearchResult.class,
	// 			store.id, store.name, store.address, category.name, similarity))
	// 		.from(store)
	// 		.leftJoin(store.category, category)
	// 		.where(whereCondition)
	// 		.orderBy(similarity.desc(), distance.asc())
	// 		.offset(pageable.getOffset())
	// 		.limit(pageable.getPageSize())
	// 		.fetch();
	//
	// 	Long total = queryFactory
	// 		.select(store.count())
	// 		.from(store)
	// 		.leftJoin(store.category, category)
	// 		.where(whereCondition)
	// 		.fetchOne();
	//
	// 	return new PageImpl<>(content, pageable, total != null ? total : 0L);
	// }
}
