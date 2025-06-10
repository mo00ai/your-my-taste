package com.example.taste.domain.board.dto.response;

import java.time.LocalDateTime;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OpenRunBoardResponseDto {
	private String userName;
	private String userImageUrl;
	private String title;
	private LocalDateTime openTime;
	private BoardStatus boardStatus;

	public static OpenRunBoardResponseDto from(Board board) {
		return OpenRunBoardResponseDto.builder()
			.userName(board.getUser().getNickname())
			.userImageUrl(board.getUser().getImage().getUrl())
			.title(board.getTitle())
			.openTime(board.getOpenTime())
			.boardStatus(board.getStatus())
			.build();
	}
}
