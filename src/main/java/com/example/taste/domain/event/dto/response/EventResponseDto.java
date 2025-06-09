package com.example.taste.domain.event.dto.response;

import java.time.LocalDate;

import com.example.taste.domain.event.entity.Event;

import lombok.Builder;
import lombok.Getter;

@Getter
public class EventResponseDto {
	private String name;
	private String contents;
	private LocalDate startDate;
	private LocalDate endDate;
	private boolean isActive;

	@Builder
	public EventResponseDto(Event event) {
		this.name = event.getName();
		this.contents = event.getContents();
		this.startDate = event.getStartDate();
		this.endDate = event.getEndDate();
		this.isActive = event.isActive();
	}
}
