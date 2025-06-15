package com.example.taste.fixtures;

import static com.example.taste.domain.board.entity.BoardType.*;

import java.time.LocalDateTime;

import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardStatus;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

public class BoardFixture {
	public static Board createOBoard(OpenRunBoardRequestDto dto, Store store, User user) {
		return Board.oBoardBuilder()
			.title("여긴 진짜 내 인생 최고로 맛있었던 곳")
			.contents("00 음식점입니다.")
			.type(O)
			.status(BoardStatus.TIMEATTACK)
			.openLimit(dto.getOpenLimit())
			.openTime(dto.getOpenTime())
			.store(store)
			.user(user)
			.end();
	}

	public static Board createOBoardWithoutDto(Store store, User user) {
		return Board.oBoardBuilder()
			.title("여긴 진짜 내 인생 최고로 맛있었던 곳")
			.contents("00 음식점입니다.")
			.type(O)
			.status(BoardStatus.TIMEATTACK)
			.openLimit(10)
			.openTime(LocalDateTime.now().plusDays(1))
			.store(store)
			.user(user)
			.end();
	}

	public static Board createClosedOBoardWithoutDto(Store store, User user) {
		return Board.oBoardBuilder()
			.title("여긴 진짜 내 인생 최고로 맛있었던 곳")
			.contents("00 음식점입니다.")
			.type(O)
			.status(BoardStatus.CLOSED)
			.openLimit(10)
			.openTime(LocalDateTime.now().plusDays(1))
			.store(store)
			.user(user)
			.end();
	}

	public static Board createTimeLimitedOBoardWithoutDto(Store store, User user) {
		return Board.oBoardBuilder()
			.title("여긴 진짜 내 인생 최고로 맛있었던 곳")
			.contents("00 음식점입니다.")
			.type(O)
			.status(BoardStatus.TIMEATTACK)
			.openLimit(10)
			.openTime(LocalDateTime.now().minusDays(1))
			.store(store)
			.user(user)
			.end();
	}

	public static Board createFcfsOBoardWithoutDto(Store store, User user) {
		return Board.oBoardBuilder()
			.title("여긴 진짜 내 인생 최고로 맛있었던 곳")
			.contents("00 음식점입니다.")
			.type(O)
			.status(BoardStatus.FCFS)
			.openLimit(1)
			.openTime(LocalDateTime.now().minusDays(1))
			.store(store)
			.user(user)
			.end();
	}
}
