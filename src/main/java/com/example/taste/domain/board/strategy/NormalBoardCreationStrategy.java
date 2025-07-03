package com.example.taste.domain.board.strategy;

import com.example.taste.domain.board.dto.request.NormalBoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

public class NormalBoardCreationStrategy implements BoardCreationStrategy<NormalBoardRequestDto> {

	@Override
	public Board createBoard(NormalBoardRequestDto requestDto, Store store, User user) {
		return Board.nBoardBuilder()
			.title(requestDto.getTitle())
			.contents(requestDto.getContents())
			.type(requestDto.getType())
			.accessPolicy(requestDto.getAccessPolicy())
			.store(store)
			.user(user)
			.buildNormal();
	}
}
