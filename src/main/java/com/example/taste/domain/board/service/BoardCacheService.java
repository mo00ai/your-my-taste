package com.example.taste.domain.board.service;

import static com.example.taste.config.CacheConfig.*;

import java.util.List;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.entity.Board;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardCacheService {

	private final CacheManager concurrentMapCacheManager;

	// 외부 클래스에서 호출해야 캐시 정상 동작(aop)
	@Cacheable(value = TIMEATTACK_CACHE_NAME, key = "#board.id")
	public BoardResponseDto getOrSetCache(Board board) {
		return new BoardResponseDto(board);
	}

	public void evictCache(List<? extends Long> ids) {
		Cache cache = concurrentMapCacheManager.getCache(TIMEATTACK_CACHE_NAME);
		if (cache != null) {
			for (Long boardId : ids) {
				cache.evict(boardId); // 개별 삭제
			}
		}
	}
}
