package com.example.taste.domain.board.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.BoardRepository;
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

	@Mock
	private BoardRepository boardRepository;

	@Test
	public void findBoard_whenNotOpenYet_ThrowException() {
		// given
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);
		Board board = BoardFixture.createOBoardWithoutDto(store, user);
		long boardId = 999L, userId = 999L;

		// stub
		given(boardRepository.findActiveBoard(anyLong())).willReturn(Optional.of(board));

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.findBoard(boardId, userId);
		});
	}

	@Test
	public void findBoard_whenClosedBoard_ThrowException() {
		// given
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);
		Board board = BoardFixture.createClosedOBoardWithoutDto(store, user);
		long boardId = 999L, userId = 999L;

		// stub
		given(boardRepository.findActiveBoard(anyLong())).willReturn(Optional.of(board));

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.findBoard(boardId, userId);
		});
	}

	@Test
	public void findBoard_whenTimeExceededBoard_ThrowException() {
		// given
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);
		Board board = BoardFixture.createTimeLimitedOBoardWithoutDto(store, user);
		long boardId = 999L, userId = 999L;

		// stub
		given(boardRepository.findActiveBoard(anyLong())).willReturn(Optional.of(board));

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.findBoard(boardId, userId);
		});
	}
}