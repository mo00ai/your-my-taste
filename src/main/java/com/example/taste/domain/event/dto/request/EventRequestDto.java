package com.example.taste.domain.event.dto.request;

import java.time.LocalDate;

import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventRequestDto {

	private String name;
	private String contents;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;
	@JsonProperty("isActive")
	private Boolean isActive;

	public static Event toEntity(User user, EventRequestDto dto) {
		return Event.builder()
			.name(dto.name)
			.contents(dto.contents)
			.startDate(dto.startDate)
			.endDate(dto.endDate)
			.isActive(dto.isActive)
			.user(user)
			.build();

	}
}
