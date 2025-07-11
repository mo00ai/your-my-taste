package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.RedisConst.*;
import static com.example.taste.domain.board.entity.AccessPolicy.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.taste.common.response.PageResponse;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.board.dto.response.OpenRunBoardResponseDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardType;
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
class BoardServiceTest extends AbstractIntegrationTest {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private BoardService boardService;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private RedisService redisService;

	@Test
	@Transactional
	void deleteBoard_whenFcfsBoard_thenZSetSizeIsZero() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.saveAndFlush(UserFixture.createNoMorePosting(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.save(
			BoardFixture.createOBoard("title", "contents", BoardType.O.name(), FCFS.name(), 10, LocalDateTime.now(),
				store, user));
		String key = FCFS_KEY_PREFIX + board.getId();
		redisService.addToZSet(key, user.getId(), System.currentTimeMillis());

		// when
		boardService.deleteBoard(user.getId(), board.getId());

		// then
		assertThat(redisService.getKeyLongValue(key)).isNull();
	}

	@Test
	@Transactional
	void findOpenRunBoardList_whenOpenedFcfs_thenResultIncludeOpenLimitAndSlot() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.saveAndFlush(UserFixture.createNoMorePosting(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.save(
			BoardFixture.createOBoard("title", "contents", BoardType.O.name(), FCFS.name(), 10, LocalDateTime.now(),
				store, user));
		Pageable pageable = PageRequest.of(0, 10);

		// when
		PageResponse<OpenRunBoardResponseDto> boardList = boardService.findOpenRunBoardList(pageable);

		// then
		OpenRunBoardResponseDto targetDto = boardList.getContent().stream()
			.filter(dto -> dto.getBoardId().equals(board.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(targetDto.getOpenLimit()).isNotNull();
		assertThat(targetDto.getRemainingSlot()).isNotNull();
	}

	@Test
	@Transactional
	void findOpenRunBoardList_whenClosedFcfs_thenResultExcludeOpenLimitAndSlot() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.saveAndFlush(UserFixture.createNoMorePosting(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.saveAndFlush(
			BoardFixture.createOBoard("title", "contents", "O", FCFS.name(), 10,
				LocalDateTime.now().plusDays(1), store, user));
		Pageable pageable = PageRequest.of(0, 10);

		// when
		PageResponse<OpenRunBoardResponseDto> boardList = boardService.findOpenRunBoardList(pageable);

		// then
		OpenRunBoardResponseDto targetDto = boardList.getContent().stream()
			.filter(dto -> dto.getBoardId().equals(board.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(targetDto.getOpenLimit()).isNull();
		assertThat(targetDto.getRemainingSlot()).isNull();
	}

	@Test
	@Transactional
	void findOpenRunBoardList_whenOpenedTimeAttack_thenResultIncludeOpenLimitNotSlot() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.saveAndFlush(UserFixture.createNoMorePosting(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.saveAndFlush(
			BoardFixture.createOBoard("title", "contents", "O", TIMEATTACK.name(), 10,
				LocalDateTime.now(),
				store, user));
		Pageable pageable = PageRequest.of(0, 10);

		// when
		PageResponse<OpenRunBoardResponseDto> boardList = boardService.findOpenRunBoardList(pageable);

		// then
		OpenRunBoardResponseDto targetDto = boardList.getContent().stream()
			.filter(dto -> dto.getBoardId().equals(board.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(targetDto.getOpenLimit()).isNotNull();
		assertThat(targetDto.getRemainingSlot()).isNull();
	}

	@Test
	@Transactional
	void findOpenRunBoardList_whenClosedTimeAttack_thenResultExcludeOpenLimitAndSlot() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.saveAndFlush(UserFixture.createNoMorePosting(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.saveAndFlush(
			BoardFixture.createOBoard("title", "contents", "O", TIMEATTACK.name(), 10,
				LocalDateTime.now().plusDays(1), store, user));
		Pageable pageable = PageRequest.of(0, 10);

		// when
		PageResponse<OpenRunBoardResponseDto> boardList = boardService.findOpenRunBoardList(pageable);

		// then
		OpenRunBoardResponseDto targetDto = boardList.getContent().stream()
			.filter(dto -> dto.getBoardId().equals(board.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(targetDto.getOpenLimit()).isNull();
		assertThat(targetDto.getRemainingSlot()).isNull();
	}
}
