package com.example.taste.domain.board.mapper;

import com.example.taste.domain.board.dto.request.NormalBoardRequestDto;
import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.dto.response.BoardListResponseDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardStatus;
import com.example.taste.domain.board.entity.BoardType;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

public class BoardMapper {

	/**
	 * 일반 게시글 DTO → Board 엔터티
	 */
	public static Board toEntity(NormalBoardRequestDto requestDto, Store store, User user) {
		return Board.builder()
			.title(requestDto.getTitle())
			.contents(requestDto.getContents())
			.type(BoardType.from(requestDto.getType()))
			.store(store)
			.user(user)
			.build();
	}

	/**
	 * 홍대병 게시글 DTO → Board 엔터티
	 */
	public static Board toEntity(OpenRunBoardRequestDto requestDto, Store store, User user) {
		return Board.oBoardBuilder()
			.title(requestDto.getTitle())
			.contents(requestDto.getContents())
			.type(BoardType.from(requestDto.getType()))
			.status(BoardStatus.from(requestDto.getStatus()))
			.openLimit(requestDto.getOpenLimit())
			.openTime(requestDto.getOpenTime())
			.store(store)
			.user(user)
			.build();
	}

	public static BoardListResponseDto toDto(Board entity) {
		return BoardListResponseDto.builder()
			.boardId(entity.getId())
			.title(entity.getTitle())
			.storeName(entity.getStore().getName())
			.writerName(entity.getUser().getNickname())
			.thumbnailImageUrl(entity.getBoardImageList().get(0).getImage().getUrl())
			.build();
	}

}
