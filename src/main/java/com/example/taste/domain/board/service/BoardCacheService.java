package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.CacheConst.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.entity.Board;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BoardCacheService {

	private final CacheManager concurrentMapCacheManager;

	public BoardCacheService(CacheManager concurrentMapCacheManager,
		@Qualifier(value = "redisCacheManager") CacheManager redisCacheManager) {
		this.concurrentMapCacheManager = concurrentMapCacheManager;
	}

	@Cacheable(value = TIMEATTACK_CACHE_NAME, key = "#board.id", condition = "!#board.accessPolicy.closed")
	public BoardResponseDto getInMemoryCache(Board board) {
		log.debug("타임어택 게시글 캐시 저장 : id = {}", board.getId());
		return new BoardResponseDto(board);
	}

	@Cacheable(value = FCFS_CACHE_NAME, key = "#board.id", condition = "#board.canCaching()", cacheManager = "redisCacheManager")
	public BoardResponseDto getRedisCache(Board board) {
		log.debug("선착순 게시글 캐시 저장 : id = {}", board.getId());
		return new BoardResponseDto(board);
	}

	// 공개시간 만료된 타임어택 게시글 캐시 삭제
	public void evictTimeAttackCaches(List<? extends Long> ids) {
		Cache cache = concurrentMapCacheManager.getCache(TIMEATTACK_CACHE_NAME);
		if (cache != null) {
			for (Long boardId : ids) {
				cache.evict(boardId); // 개별 삭제
			}
		}
		log.debug("공개 만료된 타임어택 게시글 캐시 삭제 : id = {}", ids);
	}

	@Caching(evict = {
		@CacheEvict(value = TIMEATTACK_CACHE_NAME, key = "#board.id"),
		@CacheEvict(value = FCFS_CACHE_NAME, key = "#board.id", cacheManager = "redisCacheManager")
	})
	public void evictCache(Board board) {
		log.debug("오픈런 게시글 캐시 삭제 : id = {}", board.getId());
	}
}
