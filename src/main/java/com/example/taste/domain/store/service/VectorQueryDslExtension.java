package com.example.taste.domain.store.service;

import static com.querydsl.core.types.dsl.Expressions.*;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.ArrayPath;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimpleExpression;

/**
 * QueryDSL 은 기본적으로 PostgreSQL 의 벡터 연산자를 알지 못합니다. ( <->, <=>, <#> )
 * 따라서 Expressions.simpleTemplate() 으로 "원시 SQL" 조각을 만들어 연결해줍니다.
 *
 *  - cosineDistance   : <=> 연산자는 pgvector 의 Cosine 거리(metric) 연산자.
 *  - cosineSimilarity : 1 - CosineDistance (유사도로 환산)
 *  - innerProduct     : <#> 는 내적 연산자(클수록 유사). * -1(음수) 는 QDSL orderBy ASC 대응용.
 *  - l2Distance       : <-> 는 L2(Euclidean) 거리.
 */
@Component
public final class VectorQueryDslExtension {

	// 지구 반지름 (미터)을 상수로 정의
	private static final double EARTH_RADIUS_METERS = 6371000;

	/** Cosine 거리 (<=>) : 값이 작을수록 가깝다 */
	public static SimpleExpression<Double> cosineDistance(ArrayPath<float[], Float> column,
		float[] queryVector) {
		return simpleTemplate(Double.class, "{0} <=> {1}",
			column, constant(queryVector));
	}

	/** 1 - CosineDistance : 값이 클수록 유사 (1=같음, 0=직각) */
	public static NumberExpression<Double> cosineSimilarity(ArrayPath<float[], Float> column,
		float[] queryVector) {
		return numberTemplate(Double.class,
			"1 - ({0} <=> {1})", column, constant(queryVector));
	}

	/** 내적(<#>) : pgvector 는 값이 작을수록 가까움 → -1 곱해 DESC 정렬이 쉽게 */
	public static SimpleExpression<Double> innerProduct(ArrayPath<float[], Float> column,
		float[] queryVector) {
		return simpleTemplate(Double.class,
			"({0} <#> {1}) * -1", column, constant(queryVector));
	}

	/** L2(유클리드) 거리 (<->) */
	public static SimpleExpression<Double> l2Distance(ArrayPath<float[], Float> column,
		float[] queryVector) {
		return simpleTemplate(Double.class,
			"{0} <-> {1}", column, constant(queryVector));
	}

	/**
	 * 하버사인 거리 계산 (미터 단위)
	 * DB에 생성된 haversine_distance 함수 호출
	 */
	public static NumberExpression<Double> haversineDistance(
		NumberPath<BigDecimal> lat1, NumberPath<BigDecimal> lon1,
		double lat2, double lon2) {
		return numberTemplate(Double.class,
			"haversine_distance({0}, {1}, {2}, {3})",
			lat1, lon1, constant(lat2), constant(lon2)); //constant로 상수처리
	}

	/**
	 * 반경 내 위치 필터링 (하버사인 거리 기반)
	 * 특정 반경(미터) 내에 있는지 여부를 Boolean 표현식으로 반환
	 */
	public static BooleanExpression withinRadius(
		NumberPath<BigDecimal> lat1, NumberPath<BigDecimal> lon1,
		double lat2, double lon2, int radiusInMeters) {
		return booleanTemplate(
			"haversine_distance({0}, {1}, {2}, {3}) < {4}",
			lat1, lon1, constant(lat2), constant(lon2), constant(radiusInMeters));
	}
}
