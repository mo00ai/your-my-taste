package com.example.taste.domain.event.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventUpdateRequestDto {
	private String name;
	private String contents;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate = LocalDate.now();
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate = LocalDate.now();
	@JsonProperty("isActive")
	private Boolean isActive;
}
