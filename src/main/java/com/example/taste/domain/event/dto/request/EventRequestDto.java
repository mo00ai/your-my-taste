package com.example.taste.domain.event.dto.request;

import java.time.LocalDate;

import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventRequestDto {
	@NotBlank(message = "이벤트 이름은 필수입니다")
	private String name;
	@NotBlank(message = "이벤트 내용은 필수입니다")
	private String contents;
	@NotNull(message = "시작일은 필수입니다")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;
	@NotNull(message = "종료일은 필수입니다")
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
