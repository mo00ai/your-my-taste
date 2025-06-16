package com.example.taste.fixtures;

import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

public class BoardFixture {
	public static Board createTimeAttackBoard(OpenRunBoardRequestDto dto, Store store, User user) {
		return Board.oBoardBuilder()
			.title(dto.getTitle())
			.contents(dto.getContents())
			.status(dto.getStatus())
			.type(dto.getType())
			.openLimit(dto.getOpenLimit())
			.openTime(dto.getOpenTime())
			.store(store)
			.user(user)
			.buildOpenRun();
	}

	public static Board createClosedOBoard(OpenRunBoardRequestDto dto, Store store, User user) {
		return Board.oBoardBuilder()
			.title(dto.getTitle())
			.contents(dto.getContents())
			.status(dto.getStatus())
			.type(dto.getType())
			.openLimit(dto.getOpenLimit())
			.openTime(dto.getOpenTime())
			.store(store)
			.user(user)
			.buildOpenRun();
	}

	public static Board createTimeLimitedOBoard(OpenRunBoardRequestDto dto, Store store, User user) {
		return Board.oBoardBuilder()
			.title(dto.getTitle())
			.contents(dto.getContents())
			.status(dto.getStatus())
			.type(dto.getType())
			.openLimit(dto.getOpenLimit())
			.openTime(dto.getOpenTime())
			.store(store)
			.user(user)
			.buildOpenRun();
	}

	public static Board createFcfsOBoard(OpenRunBoardRequestDto dto, Store store, User user) {
		return Board.oBoardBuilder()
			.title(dto.getTitle())
			.contents(dto.getContents())
			.status(dto.getStatus())
			.type(dto.getType())
			.openLimit(dto.getOpenLimit())
			.openTime(dto.getOpenTime())
			.store(store)
			.user(user)
			.buildOpenRun();
	}
}
