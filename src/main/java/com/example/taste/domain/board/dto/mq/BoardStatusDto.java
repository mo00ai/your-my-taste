package com.example.taste.domain.board.dto.mq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BoardStatusDto {
	private final long boardId;
	private final long remainingSlot;
}
