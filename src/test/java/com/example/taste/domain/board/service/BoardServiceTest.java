package com.example.taste.domain.board.service;

import static com.example.taste.domain.board.entity.AccessPolicy.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
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

	@Test
	@Transactional
	void createBoard_whenOpenRunTypeRequest_thenIncreasePostingCnt() throws IOException {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		// BoardRequestDto-OpenRunRequestDto의 상속구조로 강제 set 필요
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", TIMEATTACK.name());
		ReflectionTestUtils.setField(dto, "storeId", store.getId());
		ReflectionTestUtils.setField(dto, "hashtagList", List.of("맛집", "한식"));
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));

		// when
		boardService.createBoard(user.getId(), dto, new ArrayList<>());

		// then
		assertThat(userRepository.findById(user.getId()).get().getPostingCount()).isEqualTo(1);
	}

	@Test
	@Transactional
	void createBoard_whenPostingCntIsLimit_thenThrowException() throws IOException {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.createNoMorePosting(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		// BoardRequestDto-OpenRunRequestDto의 상속구조로 강제 set 필요
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", FCFS.name());
		ReflectionTestUtils.setField(dto, "storeId", store.getId());
		ReflectionTestUtils.setField(dto, "hashtagList", List.of("맛집", "한식"));
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.createBoard(user.getId(), dto, new ArrayList<>());
		});
	}

	@Test
	public void findBoard_whenRankExceededBoard_ThrowException() {
	}

	@Test
	public void findBoard_whenIsOpenedAndRankInFcfsBoard_thenSuccess() {
	}

	@Test
	void deleteBoard_whenFcfsBoard_thenZSetSizeIsZero() {
	}

	@Test
	void deleteBoard_thenCacheEvict() {
	}

	@Test
	void updateBoard_thenCacheEvict() {
	}

	@Test
	void findOpenRunBoardList() {
	}
}
