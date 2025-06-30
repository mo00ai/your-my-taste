package com.example.taste.domain.board.dto.response;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

import java.time.LocalDateTime;

import com.example.taste.domain.board.entity.AccessPolicy;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

@Getter
@JsonInclude(value = NON_NULL)
public class OpenRunBoardQueryDto {
	private final Long userId;
	private final String userName;
	private final String userImageUrl;
	private final Long boardId;
	private final String title;
	private final LocalDateTime openTime;
	private final AccessPolicy accessPolicy;
	private final int openLimit; // 접근 허용 인원 수 or 시간

	public OpenRunBoardQueryDto(Long userId, String userName, String userImageUrl, Long boardId, String title,
		LocalDateTime openTime, AccessPolicy accessPolicy, int openLimit) {
		this.userId = userId;
		this.userName = userName;
		this.userImageUrl = userImageUrl;
		this.boardId = boardId;
		this.title = title;
		this.openTime = openTime;
		this.accessPolicy = accessPolicy;
		this.openLimit = openLimit;
	}
}