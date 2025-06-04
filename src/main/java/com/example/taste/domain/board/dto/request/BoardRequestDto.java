package com.example.taste.domain.board.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,            // type으로 구분
	include = JsonTypeInfo.As.PROPERTY, // JSON 필드로 포함
	property = "type",                    // "type": "N" 또는 "H"
	visible = true                        // 서브 클래스에서도 type 사용 가능
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = NormalBoardRequestDto.class, name = "N"),
	@JsonSubTypes.Type(value = HongdaeBoardRequestDto.class, name = "H")
})
@Getter
public abstract class BoardRequestDto {
	private String title;
	private String contents;
	private String type;
	private String status;
	private Long storeId;

}
