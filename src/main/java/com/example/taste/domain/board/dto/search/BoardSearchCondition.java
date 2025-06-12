package com.example.taste.domain.board.dto.search;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.taste.common.annotation.ValidEnum;
import com.example.taste.domain.board.entity.BoardStatus;
import com.example.taste.domain.board.entity.BoardType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoardSearchCondition {
	// 키워드로 제목+내용을 동시에 검색하는 경우
	private String keyword;
	// 필드별 검색
	private String title;
	private String contents;
	private String storeName;
	private String authorName;

	// 필터링
	@ValidEnum(target = BoardType.class)
	private String type;    // "N", "O"
	@ValidEnum(target = BoardStatus.class)
	private String status;    // "OPEN", "CLOSED",  "FCFS", "TIMEATTACK"
	// 날짜 범위
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate createdFrom;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate createdTo;

}
