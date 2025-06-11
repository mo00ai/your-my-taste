package com.example.taste.domain.board.dto.response;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

import java.time.LocalDateTime;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(value = NON_NULL)
public class OpenRunBoardResponseDto {
	private Long userId;
	private String userName;
	private String userImageUrl;
	private Long boardId;
	private String title;
	private LocalDateTime openTime;
	private BoardStatus boardStatus;
	private Integer openLimit; // 접근 허용 인원 수 or 시간
	private Long remainingSlot; // 잔여 인원 수

	// 공개 예정 게시글이면 openLimit, remainingSlot에 null 삽입
	public static OpenRunBoardResponseDto create(Board board, Long remainingSlot) {
		boolean isOpened = !board.getOpenTime().isAfter(LocalDateTime.now());

		return OpenRunBoardResponseDto.builder()
			.userId(board.getUser().getId())
			.userName(board.getUser().getNickname())
			.userImageUrl(board.getUser().getImage() != null ? board.getUser().getImage().getUrl() : null)
			.boardId(board.getId())
			.title(board.getTitle())
			.openTime(board.getOpenTime())
			.boardStatus(board.getStatus())
			.openLimit(isOpened ? board.getOpenLimit() : null)
			.remainingSlot(isOpened ? remainingSlot : null)
			.build();
	}
}
