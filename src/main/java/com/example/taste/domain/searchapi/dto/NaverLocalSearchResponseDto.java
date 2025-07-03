package com.example.taste.domain.searchapi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverLocalSearchResponseDto {
	private String lastBuildDate;
	private int total;
	private int start;
	private int display;
	@JsonProperty("items")
	private List<Item> items;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Item {
		@JsonProperty("title")
		private String title;

		@JsonProperty("link")
		private String link;

		@JsonProperty("category")
		private String category;

		@JsonProperty("description")
		private String description;

		@JsonProperty("telephone")
		private String telephone;

		@JsonProperty("address")
		private String address;

		@JsonProperty("roadAddress")
		private String roadAddress;

		@JsonProperty("mapx")
		private String mapx;

		@JsonProperty("mapy")
		private String mapy;
	}
}
