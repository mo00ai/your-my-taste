package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.RedisConst.*;
import static com.example.taste.domain.board.exception.BoardErrorCode.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.BoardRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardCacheService {

	private final BoardRepository boardRepository;
	private final MeterRegistry registry;
	private final RedisService redisService;
	private Counter counter;

	@PostConstruct
	public void initCounter() {
		counter = Counter.builder("cache.hit.success")
			.description("캐시 히트 성공량")
			.register(registry);
	}

	// 일반 게시글은 캐싱 안하므로 boardResponseDto 타입으로 반환
	public BoardResponseDto getOrSetCache(Long boardId) {

		BoardResponseDto cachedBoardDto = (BoardResponseDto)redisService.getKeyValue(CACHE_KEY_PREFIX + boardId);
		if (cachedBoardDto != null) {
			counter.increment();
			return cachedBoardDto;
		}

		Board board = boardRepository.findActiveBoard(boardId).orElseThrow(() -> new CustomException(BOARD_NOT_FOUND));
		if (board.isNBoard()) {
			return new BoardResponseDto(board);
		}

		Duration duration = Duration.between(LocalDateTime.now(),
			board.getOpenTime().plusMinutes(board.getOpenLimit()));
		if (board.getAccessPolicy().isFcfs()) {
			duration = DEFAULT_TTL;
		}
		if (duration.isNegative() || duration.isZero()) {
			throw new CustomException(CLOSED_BOARD);
		}

		cachedBoardDto = new BoardResponseDto(board);
		redisService.setKeyValue(CACHE_KEY_PREFIX + boardId, cachedBoardDto, duration);
		return cachedBoardDto;
	}

	// 공개시간 만료된 타임어택 게시글 캐시 삭제
	public void evictTimeAttackCaches(List<? extends Long> ids) {
		for (Long boardId : ids) {
			redisService.deleteKey(CACHE_KEY_PREFIX + boardId);
		}
		log.debug("공개 만료된 타임어택 게시글 캐시 삭제 : id = {}", ids);
	}

	public void evictCache(Board board) {
		redisService.deleteKey(CACHE_KEY_PREFIX + board.getId());
		log.debug("오픈런 게시글 캐시 삭제 : id = {}", board.getId());
	}
}
