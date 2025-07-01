package com.example.taste.domain.embedding.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.config.KoreanTextProcessor;
import com.example.taste.domain.store.init.CategoryManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

	private final EmbeddingModel embeddingModel;
	private final KoreanTextProcessor processor;
	private final CategoryManager categoryManager;

	// 단순 임베딩 벡터 생성 메서드
	public float[] createEmbedding(String text) {
		if (text == null || text.trim().isEmpty()) {
			throw new CustomException(ErrorCode.EMBEDDING_TEXT_NOT_FOUND);
		}
		return embeddingModel.embed(text);

	}

	public List<float[]> createEmbeddingBatch(List<String> inputs) {
		if (inputs == null || inputs.isEmpty()) {
			throw new CustomException(ErrorCode.EMBEDDING_TEXT_NOT_FOUND);
		}
		return embeddingModel.embed(inputs);   // Spring AI 1.0+ 는 멀티 입력 지원
	}

	public void stemming(String text) {
		String normalize = processor.normalize(text);
		System.out.println("normalize = " + normalize);

		List<String> Nouns = processor.extractNouns(text);

		List<String> phrases = processor.extractPhrases(text);

		Set<String> keywords = processor.extractSearchKeywords(text);
		System.out.println("keywords = " + keywords);

	}

	/**
	 * 임베딩 생성 (시간 측정 포함)
	 */
	public void createEmbeddingTest(String text) {
		log.info("embeddingModelClass: {}", embeddingModel.getClass().getSimpleName());
		log.info("=== 임베딩 생성 시간 측정 시작 ===");

		long totalStartTime = System.nanoTime();

		try {
			// 1. 텍스트 정규화 시간 측정
			long normalizeStartTime = System.nanoTime();
			String normalizedText = processor.normalize(text);
			long normalizeEndTime = System.nanoTime();
			double normalizeTimeMs = (normalizeEndTime - normalizeStartTime) / 1_000_000.0;

			log.info("1. 텍스트 정규화 시간: {}ms", String.format("%.2f", normalizeTimeMs));
			log.info("   원본: {}", text);
			log.info("   정규화: {}", normalizedText);

			// 2. 텍스트 토큰화 시간 측정 (키워드 추출)
			long tokenizeStartTime = System.nanoTime();
			Set<String> keywords = processor.extractSearchKeywords(normalizedText);
			String processedText = String.join(" ", keywords);
			long tokenizeEndTime = System.nanoTime();
			double tokenizeTimeMs = (tokenizeEndTime - tokenizeStartTime) / 1_000_000.0;

			log.info("2. 텍스트 토큰화 시간: {}ms", String.format("%.2f", tokenizeTimeMs));
			log.info("   추출된 키워드: {}", keywords);
			log.info("   토큰화된 텍스트: {}", processedText);

			// 3. 토큰화된 텍스트 임베딩 시간 측정
			long embeddingStartTime = System.nanoTime();
			float[] embedding = embeddingModel.embed(processedText);
			long embeddingEndTime = System.nanoTime();
			double embeddingTimeMs = (embeddingEndTime - embeddingStartTime) / 1_000_000.0;

			log.info("3. 토큰화된 텍스트 임베딩 시간: {}ms", String.format("%.2f", embeddingTimeMs));
			log.info("   임베딩 차원: {}", embedding.length);
			log.info("   임베딩 미리보기: {}",
				Arrays.toString(Arrays.copyOfRange(embedding, 0, Math.min(10, embedding.length))));

			// 4. 전체 시간 계산
			long totalEndTime = System.nanoTime();
			double totalTimeMs = (totalEndTime - totalStartTime) / 1_000_000.0;

			log.info("=== 임베딩 생성 시간 측정 완료 ===");
			log.info("전체 처리 시간: {}ms", String.format("%.2f", totalTimeMs));
			log.info("시간 분배 - 정규화: {}%, 토큰화: {}%, 임베딩: {}%",
				String.format("%.1f", (normalizeTimeMs / totalTimeMs) * 100),
				String.format("%.1f", (tokenizeTimeMs / totalTimeMs) * 100),
				String.format("%.1f", (embeddingTimeMs / totalTimeMs) * 100));

			System.out.println(processor.koreanTokenize(normalizedText));
		} catch (Exception e) {
			log.error("임베딩 생성 실패: {}", e.getMessage(), e);
		}
	}

	// 검색용 쿼리 정규화 및 임베딩 벡터 생성
	public float[] search(String query) {
		// 핵심 키워드 추출(정규화 및 토큰화 & 필터링)
		Set<String> keywords = processor.extractSearchKeywords(query);
		for (String keyword : keywords) {
			log.info("keyword :{}", keyword);
		}
		String buildWeightedText = buildWeightedText(keywords);
		log.info("buildWeightedText: {}", buildWeightedText);
		return embeddingModel.embed(buildWeightedText);
	}

	// 위치 정보 및 카테고리 정보 가중치 추가
	private String buildWeightedText(Set<String> keywords) {
		// 가중치 상수
		final int LOCATION_WEIGHT = 2;
		final int CATEGORY_WEIGHT = 3;
		final int NORMAL_WEIGHT = 1;

		StringBuilder result = new StringBuilder();

		// 위치와 카테고리 키워드 분류
		Set<String> locationKeywords = new HashSet<>();
		Set<String> categoryKeywords = new HashSet<>();
		Set<String> normalKeywords = new HashSet<>();

		for (String keyword : keywords) {
			if (processor.isLocationKeyword(keyword)) {
				locationKeywords.add(keyword);
			} else if (categoryManager.isFoodCategory(keyword)) {
				categoryKeywords.add(keyword);
			} else {
				normalKeywords.add(keyword);
			}
		}

		// 각 키워드 그룹에 가중치 적용
		appendKeywordsWithWeight(result, locationKeywords, LOCATION_WEIGHT);
		appendKeywordsWithWeight(result, categoryKeywords, CATEGORY_WEIGHT);
		appendKeywordsWithWeight(result, normalKeywords, NORMAL_WEIGHT);

		return result.toString().trim();
	}

	// 키워드 세트에 가중치를 적용하여 StringBuilder에 추가
	private void appendKeywordsWithWeight(StringBuilder sb, Set<String> keywords, int weight) {
		if (keywords.isEmpty())
			return;
		// 하나로 합침
		String keywordText = String.join(" ", keywords);
		appendWithWeight(sb, keywordText, weight);
	}

	// 텍스트에 가중치를 적용하여 합친 키워드 *가중치를 StringBuilder에 추가
	private void appendWithWeight(StringBuilder sb, String text, int weight) {
		if (text == null || text.trim().isEmpty())
			return;

		String textWithSpace = text.trim() + " ";
		for (int i = 0; i < weight; i++) {
			sb.append(textWithSpace);
		}
	}

}
