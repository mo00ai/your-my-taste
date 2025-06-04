package com.example.taste.domain.board.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.example.taste.domain.board.entity.Board;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BoardResponseDto {
	private final Long boardId;
	private final String title;
	private final String contents;
	private final String type;
	private final Long writerId;
	private final String writerName;
	private final List<String> imageUrlList;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	@Builder
	public BoardResponseDto(Board entity) {
		this.boardId = entity.getId();
		this.title = entity.getTitle();
		this.contents = entity.getContents();
		this.type = entity.getType().getCode();
		this.writerId = entity.getUser().getId();
		this.writerName = entity.getUser().getNickname();
		this.imageUrlList = entity.getBoardImageList().stream()
			.map(bi -> bi.getImage().getUrl())
			.toList();
		this.createdAt = entity.getCreatedAt();
		this.updatedAt = entity.getUpdatedAt();
	}

}
