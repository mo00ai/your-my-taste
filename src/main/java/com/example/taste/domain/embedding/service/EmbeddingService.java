package com.example.taste.domain.embedding.service;

import java.util.Arrays;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

	private final EmbeddingModel embeddingModel;

	public void createEmbedding(String text) {
		log.info("embeddingModelClass: {}", embeddingModel.getClass().getSimpleName());

		try {
			float[] embedding = embeddingModel.embed(text);
			log.info("embeddingDimension: {}", embedding.length);
			log.info("embeddingPreview: {}", Arrays.toString(Arrays.copyOfRange(embedding, 0, 10)));

		} catch (Exception e) {
			log.info("embeddingTest", "FAILED: " + e.getMessage());
		}
	}
}
