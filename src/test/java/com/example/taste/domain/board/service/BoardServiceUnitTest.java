package com.example.taste.domain.board.service;

import static com.example.taste.domain.board.entity.AccessPolicy.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;
import com.example.taste.fixtures.BoardFixture;
import com.example.taste.fixtures.CategoryFixture;
import com.example.taste.fixtures.ImageFixture;
import com.example.taste.fixtures.StoreFixture;
import com.example.taste.fixtures.UserFixture;

@ExtendWith(MockitoExtension.class)
public class BoardServiceUnitTest {
	@InjectMocks
	private BoardService boardService;

	@Test
	public void validateBoard_whenNotOpenYet_ThrowException() {
		// given
		Image image = ImageFixture.create();
		User user1 = UserFixture.create(image);
		User user2 = UserFixture.create(image);
		ReflectionTestUtils.setField(user1, "id", 1L);
		ReflectionTestUtils.setField(user2, "id", 2L);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", TIMEATTACK.name());
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));

		Board board = BoardFixture.createOBoard(dto, store, user1);

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.validateBoard(board, user2);
		});
	}

	@Test
	public void validateBoard_whenClosedBoard_ThrowException() {
		// given
		Image image = ImageFixture.create();
		User user1 = UserFixture.create(image);
		User user2 = UserFixture.create(image);
		ReflectionTestUtils.setField(user1, "id", 1L);
		ReflectionTestUtils.setField(user2, "id", 2L);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", CLOSED.name());
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));

		Board board = BoardFixture.createOBoard(dto, store, user1);

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.validateBoard(board, user2);
		});
	}

	@Test
	public void validateBoard_whenTimeExceededBoard_ThrowException() {
		// given
		Image image = ImageFixture.create();
		User user1 = UserFixture.create(image);
		User user2 = UserFixture.create(image);
		ReflectionTestUtils.setField(user1, "id", 1L);
		ReflectionTestUtils.setField(user2, "id", 2L);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", TIMEATTACK.name());
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().minusDays(1));

		Board board = BoardFixture.createOBoard(dto, store, user1);

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.validateBoard(board, user2);
		});
	}
}
