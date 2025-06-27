package com.example.taste.domain.store.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.taste.domain.embedding.service.EmbeddingService;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreEmbeddingUpdater {

	private final StoreRepository storeRepository;
	private final StoreService storeService;          // buildStoreEmbeddingText() 재활용
	private final EmbeddingService embeddingService;  // createEmbeddingBatch(List<String>)
	private final TransactionTemplate txTemplate;     // 프로그래매틱 트랜잭션
	private final ThreadPoolTaskExecutor executor;

	/** 호출 진입점 */
	public void fillEmptyEmbeddings() {
		int page = 0, size = 200;

		Page<Store> slice;
		do {
			slice = storeRepository.findByEmbeddingVectorIsNullWithCategory(
				PageRequest.of(page, size));
			List<Store> stores = slice.getContent();

			// 50개씩 더 잘게 나눠서 태스크 생성
			List<List<Store>> chunks = new ArrayList<>();
			for (int i = 0; i < stores.size(); i += 50) {
				chunks.add(stores.subList(i, Math.min(stores.size(), i + 50)));
			}

			List<CompletableFuture<Void>> futures = chunks.stream()
				.map(list -> CompletableFuture.runAsync(
					() -> saveChunkWithTx(list), executor))
				.toList();

			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
				.join();               // 현재 page 끝날 때까지 대기
			page++;
		} while (!slice.isLast());
	}

	/** 한 chunk(50개) = 한 트랜잭션 */
	private void saveChunkWithTx(List<Store> chunk) {
		txTemplate.executeWithoutResult(txStatus -> {
			try {
				List<String> inputs = chunk.stream()
					.map(storeService::buildStoreEmbeddingText)
					.toList();

				List<float[]> vectors =
					embeddingService.createEmbeddingBatch(inputs); // 멀티 입력

				for (int i = 0; i < chunk.size(); i++) {
					chunk.get(i).setEmbeddingVector(vectors.get(i));
				}

				storeRepository.saveAll(chunk);  // 벌크 UPDATE
			} catch (Exception e) {
				log.error("Embedding 실패 → 롤백", e);
				throw e;   // rollback
			}
		});
	}

}
