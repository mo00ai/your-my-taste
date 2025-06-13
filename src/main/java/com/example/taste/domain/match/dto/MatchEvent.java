package com.example.taste.domain.match.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.taste.domain.match.enums.MatchJobType;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MatchEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private MatchJobType matchJobType;        // 매칭 작업 타입
	private List<Long> userMatchCondIds;                        // 유저 매칭인 경우, 유저 ID

	@Builder
	public MatchEvent(MatchJobType matchJobType) {
		this.matchJobType = matchJobType;
		this.userMatchCondIds = List.of(-1L);
	}
}
