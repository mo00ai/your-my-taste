package com.example.taste.domain.board.strategy;

import com.example.taste.domain.board.dto.request.BoardRequestDto;
import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

public class OpenRunBoardCreationStrategy implements BoardCreationStrategy {
	@Override
	public Board createBoard(BoardRequestDto dto, Store store, User user) {

		OpenRunBoardRequestDto requestDto = (OpenRunBoardRequestDto)dto;
		return Board.oBoardBuilder()
			.requestDto(requestDto)
			.store(store)
			.user(user)
			.buildOpenRun();
	}
}
