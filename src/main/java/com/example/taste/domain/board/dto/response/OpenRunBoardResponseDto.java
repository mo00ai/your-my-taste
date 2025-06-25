package com.example.taste.domain.board.dto.response;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

import java.time.LocalDateTime;

import com.example.taste.domain.board.entity.AccessPolicy;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

@Getter
@JsonInclude(value = NON_NULL)
public class OpenRunBoardResponseDto {
	private final Long userId;
	private final String userName;
	private final String userImageUrl;
	private final Long boardId;
	private final String title;
	private final LocalDateTime openTime;
	private final AccessPolicy accessPolicy;
	private final Integer openLimit; // 접근 허용 인원 수 or 시간
	private Long remainingSlot; // 잔여 인원 수

	// 공개 예정 게시글이면 openLimit, remainingSlot에 null 삽입
	public OpenRunBoardResponseDto(Long userId, String userName, String userImageUrl, Long boardId, String title,
		LocalDateTime openTime, AccessPolicy accessPolicy, Integer openLimit) {
		boolean isOpened = !openTime.isAfter(LocalDateTime.now());

		this.userId = userId;
		this.userName = userName;
		this.userImageUrl = userImageUrl;
		this.boardId = boardId;
		this.title = title;
		this.openTime = openTime;
		this.accessPolicy = accessPolicy;
		this.openLimit = isOpened ? openLimit : null;
	}

	public void setRemainingSlot(Long remainingSlot) {
		boolean isOpened = !openTime.isAfter(LocalDateTime.now());
		this.remainingSlot = isOpened ? remainingSlot : null;
	}
}
