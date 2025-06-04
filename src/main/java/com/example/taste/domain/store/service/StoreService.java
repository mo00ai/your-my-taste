package com.example.taste.domain.store.service;

import static com.example.taste.domain.store.exception.StoreErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.store.dto.response.StoreResponse;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

	private final StoreRepository storeRepository;

	public StoreResponse getStore(Long id) {
		Store store = storeRepository.findById(id).orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

		List<String> imageUrls = store.getReviewList().stream()
			.sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
			.limit(3)
			.map(review -> review.getImage().getUrl())
			.toList();

		return StoreResponse.from(store, imageUrls);
	}

	@Transactional
	public void deleteStore(Long id) {
		Store store = storeRepository.findById(id).orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
		storeRepository.delete(store);
	}

	@Transactional(readOnly = true)
	public Store findById(Long storeId) {
		return storeRepository.findById(storeId)
			.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
	}
}
