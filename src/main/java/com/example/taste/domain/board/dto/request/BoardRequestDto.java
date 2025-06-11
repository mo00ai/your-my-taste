package com.example.taste.domain.board.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

	// (NO) 라고 적으면 'N' 또는 'O' 가 아니라 "NO"만 허용하게 됨 - 황기하
	@Pattern(regexp = "^[NO]$", message = "게시글 타입은 'N' 또는 'O'만 허용됩니다.")
	private String type;

	private String status;

	private Long storeId;
	// 해시 태그 추가
	private List<@NotBlank(message = "해시태그는 빈 문자열일 수 없습니다.") String> hashtagList;

}
