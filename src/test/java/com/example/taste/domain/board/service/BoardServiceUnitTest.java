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
import com.example.taste.domain.board.dto.response.BoardResponseDto;
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
		Board board = BoardFixture.createOBoard("title", "contents", "O", TIMEATTACK.name(), 10,
			LocalDateTime.now().plusDays(1), store, user1);
		BoardResponseDto dto = new BoardResponseDto(board);

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.validateOBoard(dto, user2);
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
		Board board = BoardFixture.createOBoard("title", "contents", "O", CLOSED.name(), 10,
			LocalDateTime.now().plusDays(1), store, user1);
		BoardResponseDto dto = new BoardResponseDto(board);

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.validateOBoard(dto, user2);
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
		Board board = BoardFixture.createOBoard("title", "contents", "O", TIMEATTACK.name(), 10,
			LocalDateTime.now().minusDays(1), store, user1);
		BoardResponseDto dto = new BoardResponseDto(board);

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.validateOBoard(dto, user2);
		});
	}
}
