package com.example.taste.domain.board.strategy;

import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

public class OpenRunBoardCreationStrategy implements BoardCreationStrategy<OpenRunBoardRequestDto> {

	@Override
	public Board createBoard(OpenRunBoardRequestDto requestDto, Store store, User user) {
		return Board.oBoardBuilder()
			.title(requestDto.getTitle())
			.contents(requestDto.getContents())
			.type(requestDto.getType())
			.status(requestDto.getStatus())
			.openLimit(requestDto.getOpenLimit())
			.openTime(requestDto.getOpenTime())
			.store(store)
			.user(user)
			.buildOpenRun();
	}
}
