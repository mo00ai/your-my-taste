package com.example.taste.domain.board.dto.request;

import java.util.List;

import com.example.taste.common.annotation.ValidEnum;
import com.example.taste.domain.board.entity.BoardStatus;
import com.example.taste.domain.board.entity.BoardType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,            // type으로 구분
	include = JsonTypeInfo.As.PROPERTY, // JSON 필드로 포함
	property = "type",                    // "type": "N" 또는 "O"
	visible = true                        // 서브 클래스에서도 type 사용 가능
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = NormalBoardRequestDto.class, name = "N"),
	@JsonSubTypes.Type(value = OpenRunBoardRequestDto.class, name = "O")
})
@Getter
public abstract class BoardRequestDto {
	@NotBlank(message = "제목은 필수입니다.")
	@Size(max = 100, message = "제목은 100자 이내여야 합니다.")
	private String title;

	@NotBlank(message = "내용은 필수입니다.")
	@Size(max = 1000, message = "내용은 1000자 이내여야 합니다.")
	private String contents;

	@ValidEnum(target = BoardType.class)
	private String type;
	@ValidEnum(target = BoardStatus.class)
	private String status;

	private Long storeId;
	// 해시 태그 추가
	private List<@NotBlank(message = "해시태그는 빈 문자열일 수 없습니다.") String> hashtagList;

}
