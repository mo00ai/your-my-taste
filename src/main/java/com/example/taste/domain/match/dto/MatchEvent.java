package com.example.taste.domain.match.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;

import com.example.taste.domain.match.enums.MatchJobType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Getter
public class MatchEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private MatchJobType matchJobType;        // 매칭 작업 타입
	private List<Long> userMatchInfoIdList;                        // 유저 매칭인 경우, 유저 ID

	@JsonCreator
	public MatchEvent(@JsonProperty("matchJobType") MatchJobType matchJobType,
		@JsonProperty("userMatchInfoIdList") List<Long> userMatchInfoIdList) {
		this.matchJobType = matchJobType;
		this.userMatchInfoIdList = userMatchInfoIdList != null ? userMatchInfoIdList : List.of(-1L);
	}
}
