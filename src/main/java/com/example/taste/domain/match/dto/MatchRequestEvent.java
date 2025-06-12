package com.example.taste.domain.match.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.taste.domain.match.enums.MatchJobType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchRequestEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private MatchJobType matchJobType;        // 매칭 작업 타입
	private Long userId;                        // 유저 매칭인 경우, 유저 ID
}
