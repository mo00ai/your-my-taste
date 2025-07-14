package com.example.taste.domain.board.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.example.taste.domain.board.entity.AccessPolicy;
import com.example.taste.domain.board.entity.Board;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class BoardResponseDto {
	private Long boardId;
	private String title;
	private String contents;
	private String type;
	private Long writerId;
	private String writerName;
	private List<String> imageUrlList;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private AccessPolicy accessPolicy;
	private LocalDateTime openTime;
	private Integer openLimit;

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
		this.accessPolicy = entity.getAccessPolicy();
		this.openTime = entity.isNBoard() ? null : entity.getOpenTime();
		this.openLimit = entity.isNBoard() ? null : entity.getOpenLimit();
	}
}
