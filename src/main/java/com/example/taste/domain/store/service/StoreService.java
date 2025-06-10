package com.example.taste.domain.store.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.store.dto.response.StoreResponse;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.StoreRepository;

@Service
@RequiredArgsConstructor
public class StoreService {
	private final EntityFetcher entityFetcher;
	private final StoreRepository storeRepository;

	public StoreResponse getStore(Long id) {
		Store store = entityFetcher.getStoreOrThrow(id);

		List<String> imageUrls = store.getReviewList().stream()
			.sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
			.filter(review -> review.getImage() != null)
			.limit(3)
			.map(review -> review.getImage().getUrl())
			.toList();

		return StoreResponse.create(store, imageUrls);
	}

	@Transactional
	public void deleteStore(Long id) {
		Store store = entityFetcher.getStoreOrThrow(id);
		storeRepository.delete(store);
	}

	@Transactional(readOnly = true)
	public Store findById(Long storeId) {
		return entityFetcher.getStoreOrThrow(storeId);
	}
}
