package com.example.taste.fixtures;

import java.time.LocalDateTime;

import com.example.taste.domain.board.dto.request.NormalBoardRequestDto;
import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

public class BoardFixture {
	public static Board createOBoard(OpenRunBoardRequestDto dto, Store store, User user) {
		return Board.oBoardBuilder()
			.title(dto.getTitle())
			.contents(dto.getContents())
			.accessPolicy(dto.getAccessPolicy())
			.type(dto.getType())
			.openLimit(dto.getOpenLimit())
			.openTime(dto.getOpenTime())
			.store(store)
			.user(user)
			.buildOpenRun();
	}

	public static Board createOBoard(String title, String contents, String type, String accessPolicy,
		Integer openLimit, LocalDateTime openTime, Store store, User user) {
		return Board.oBoardBuilder()
			.title(title)
			.contents(contents)
			.accessPolicy(accessPolicy)
			.type(type)
			.openLimit(openLimit)
			.openTime(openTime)
			.store(store)
			.user(user)
			.buildOpenRun();
	}

	public static Board createTimeAttackBoard(OpenRunBoardRequestDto dto, Store store, User user) {
		return Board.oBoardBuilder()
			.title(dto.getTitle())
			.contents(dto.getContents())
			.accessPolicy(dto.getAccessPolicy())
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
			.accessPolicy(dto.getAccessPolicy())
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
			.accessPolicy(dto.getAccessPolicy())
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
			.accessPolicy(dto.getAccessPolicy())
			.type(dto.getType())
			.openLimit(dto.getOpenLimit())
			.openTime(dto.getOpenTime())
			.store(store)
			.user(user)
			.buildOpenRun();
	}

	public static Board createNormalBoard(NormalBoardRequestDto dto, Store store, User user) {
		return Board.nBoardBuilder()
			.title(dto.getTitle())
			.contents(dto.getContents())
			.type(dto.getType())
			.accessPolicy(dto.getAccessPolicy())
			.store(store)
			.user(user)
			.buildNormal();
	}
}
