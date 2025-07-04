package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.RedisConst.*;
import static com.example.taste.domain.board.entity.AccessPolicy.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
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
	@Autowired
	private RedisService redisService;

	@Test
	@Transactional
	void getOrSetCache_whenCacheEmpty_thenSaveCacheAndReturn() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.saveAndFlush(
			BoardFixture.createOBoard("title", "contents", "O", TIMEATTACK.name(), 10, LocalDateTime.now(), store,
				user));

		// when
		BoardResponseDto result = boardCacheService.getOrSetCache(board.getId());

		// then
		assertThat(result.getBoardId()).isEqualTo(board.getId());
		assertThat(redisService.getKeyValue(CACHE_KEY_PREFIX + board.getId())).isEqualTo(result);

		// clean-up
		redisService.deleteKey(CACHE_KEY_PREFIX + board.getId());
	}

	@Test
	@Transactional
	void getOrSetCache_whenClosedBoard_thenNotSaveCache() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.saveAndFlush(
			BoardFixture.createOBoard("title", "contents", "O", CLOSED.name(), 10, LocalDateTime.now().minusDays(1),
				store, user));

		// when, then
		assertThrows(CustomException.class, () -> {
			boardCacheService.getOrSetCache(board.getId());
		});
		assertThat(redisService.getKeyValue(CACHE_KEY_PREFIX + board.getId())).isNull();
	}

	@Test
	@Transactional
	void evictTimeAttackCaches_success() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.saveAndFlush(
			BoardFixture.createOBoard("title", "contents", "O", TIMEATTACK.name(), 10,
				LocalDateTime.now().minusMinutes(10), store, user));

		redisService.setKeyValue(CACHE_KEY_PREFIX + board.getId(), new BoardResponseDto(board),
			Duration.between(LocalDateTime.now(),
				board.getOpenTime().plusMinutes(board.getOpenLimit())));

		// when
		boardCacheService.evictTimeAttackCaches(List.of(board.getId()));

		// then
		assertThat(redisService.getKeyValue(CACHE_KEY_PREFIX + board.getId())).isNull();
	}

	@Test
	@Transactional
	void evictCache_success() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.saveAndFlush(
			BoardFixture.createOBoard("title", "contents", "O", TIMEATTACK.name(), 10,
				LocalDateTime.now().minusMinutes(10), store, user));

		redisService.setKeyValue(CACHE_KEY_PREFIX + board.getId(), new BoardResponseDto(board),
			Duration.between(LocalDateTime.now(),
				board.getOpenTime().plusMinutes(board.getOpenLimit())));

		// when
		boardCacheService.evictCache(board);

		// then
		assertThat(redisService.getKeyValue(CACHE_KEY_PREFIX + board.getId())).isNull();
	}
}