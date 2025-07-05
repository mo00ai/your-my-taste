package com.example.taste.domain.board.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.example.taste.domain.board.entity.Board;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // redisCache 역직렬화할 때 필요
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
