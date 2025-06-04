package com.example.taste.domain.board.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BoardListResponseDto {

	private final Long boardId;
	private final String title;
	private final String storeName;
	private final String writerName;
	private final String thumbnailImageUrl;

	@Builder
	public BoardListResponseDto(Long boardId, String title, String storeName, String writerName,
		String thumbnailImageUrl) {
		this.boardId = boardId;
		this.title = title;
		this.storeName = storeName;
		this.writerName = writerName;
		this.thumbnailImageUrl = thumbnailImageUrl;
	}
}
