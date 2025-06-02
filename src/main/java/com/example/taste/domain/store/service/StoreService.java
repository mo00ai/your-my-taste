package com.example.taste.domain.store.service;

import static com.example.taste.domain.store.exception.StoreErrorCode.*;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.store.dto.response.StoreResponse;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

	private final StoreRepository storeRepository;

	@Override
	public StoreResponse getStore(Long id) {
		Store store = storeRepository.findById(id).orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
		return StoreResponse.from(store);
	}
}
