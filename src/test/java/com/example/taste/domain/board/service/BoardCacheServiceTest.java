package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.CacheConst.*;
import static com.example.taste.domain.board.entity.AccessPolicy.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.BoardFixture;
import com.example.taste.fixtures.CategoryFixture;
import com.example.taste.fixtures.ImageFixture;
import com.example.taste.fixtures.StoreFixture;
import com.example.taste.fixtures.UserFixture;
import com.example.taste.property.AbstractIntegrationTest;

import jakarta.transaction.Transactional;

@SpringBootTest
class BoardCacheServiceTest extends AbstractIntegrationTest {

	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private BoardCacheService boardCacheService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private CacheManager cacheManager;
	@Autowired
	@Qualifier(value = "redisCacheManager")
	private CacheManager redisCacheManager;

	@Test
	@Transactional
	void getInMemoryCache_whenCacheEmpty_thenSaveCacheAndReturn() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", TIMEATTACK.name());
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now());
		Board board = boardRepository.saveAndFlush(BoardFixture.createOBoard(dto, store, user));

		// when
		BoardResponseDto result = boardCacheService.getInMemoryCache(board);

		// then
		assertThat(result.getBoardId()).isEqualTo(board.getId());
		assertThat(cacheManager.getCache(TIMEATTACK_CACHE_NAME).get(board.getId()).get()).isEqualTo(result);

		// clean-up
		cacheManager.getCache(TIMEATTACK_CACHE_NAME).evictIfPresent(board.getId());
	}

	@Test
	@Transactional
	void getInMemoryCache_whenClosedBoard_thenNotSaveCache() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", CLOSED.name());
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now());
		Board board = boardRepository.saveAndFlush(BoardFixture.createOBoard(dto, store, user));

		// when
		BoardResponseDto result = boardCacheService.getInMemoryCache(board);

		// then
		assertThat(result.getBoardId()).isEqualTo(board.getId());
		assertThat(cacheManager.getCache(TIMEATTACK_CACHE_NAME).get(board.getId())).isNull();
	}

	@Test
	@Transactional
	void getRedisCache_whenExpiredTTL_thenNotSaveCache() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", FCFS.name());
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().minusMinutes(10));
		Board board = boardRepository.saveAndFlush(BoardFixture.createOBoard(dto, store, user));

		// when
		BoardResponseDto result = boardCacheService.getRedisCache(board);

		// then
		assertThat(result.getBoardId()).isEqualTo(board.getId());
		assertThat(redisCacheManager.getCache(FCFS_CACHE_NAME).get(board.getId())).isNull();
	}

	@Test
	@Transactional
	void evictTimeAttackCaches_success() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", TIMEATTACK.name());
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().minusMinutes(10));
		Board board = boardRepository.saveAndFlush(BoardFixture.createOBoard(dto, store, user));

		cacheManager.getCache(TIMEATTACK_CACHE_NAME).put(board.getId(), new BoardResponseDto(board));

		// when
		boardCacheService.evictTimeAttackCaches(List.of(board.getId()));

		// then
		assertThat(cacheManager.getCache(TIMEATTACK_CACHE_NAME).get(board.getId())).isNull();
	}

	@Test
	@Transactional
	void evictCache_whenInputTimeAttackBoard_thenEvictInMemoryCache() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", TIMEATTACK.name());
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().minusMinutes(10));
		Board board = boardRepository.saveAndFlush(BoardFixture.createOBoard(dto, store, user));

		cacheManager.getCache(TIMEATTACK_CACHE_NAME).put(board.getId(), new BoardResponseDto(board));

		// when
		boardCacheService.evictCache(board);

		// then
		assertThat(cacheManager.getCache(TIMEATTACK_CACHE_NAME).get(board.getId())).isNull();
	}

	@Test
	@Transactional
	void evictCache_whenInputFcfsBoard_thenEvictRedisCache() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", FCFS.name());
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().minusMinutes(10));
		Board board = boardRepository.saveAndFlush(BoardFixture.createOBoard(dto, store, user));

		redisCacheManager.getCache(FCFS_CACHE_NAME).put(board.getId(), new BoardResponseDto(board));

		// when
		boardCacheService.evictCache(board);

		// then
		assertThat(redisCacheManager.getCache(FCFS_CACHE_NAME).get(board.getId())).isNull();
	}
}