package com.example.taste.domain.board.dto.search;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.taste.common.annotation.ValidDateRange;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@ValidDateRange
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatedDateRange {
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate createdFrom;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate createdTo;
}
