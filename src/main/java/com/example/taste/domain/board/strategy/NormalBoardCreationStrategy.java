package com.example.taste.domain.board.strategy;

import com.example.taste.domain.board.dto.request.BoardRequestDto;
import com.example.taste.domain.board.dto.request.NormalBoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

public class NormalBoardCreationStrategy implements BoardCreationStrategy {

	@Override
	public Board createBoard(BoardRequestDto dto, Store store, User user) {
		NormalBoardRequestDto requestDto = (NormalBoardRequestDto)dto;

		return Board.nBoardBuilder()
			.requestDto(requestDto)
			.store(store)
			.user(user)
			.buildNormal();
	}
}
