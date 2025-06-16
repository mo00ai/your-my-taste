package com.example.taste.domain.board.strategy;

import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

public class OpenRunBoardCreationStrategy implements BoardCreationStrategy<OpenRunBoardRequestDto> {

	@Override
	public Board createBoard(OpenRunBoardRequestDto requestDto, Store store, User user) {
		return Board.oBoardBuilder()
			.requestDto(requestDto)
			.store(store)
			.user(user)
			.buildOpenRun();
	}
}
