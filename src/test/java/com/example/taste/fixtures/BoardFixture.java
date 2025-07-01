package com.example.taste.fixtures;

import java.time.LocalDateTime;

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
}
