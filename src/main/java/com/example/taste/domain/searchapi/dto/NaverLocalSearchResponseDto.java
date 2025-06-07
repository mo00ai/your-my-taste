package com.example.taste.domain.searchapi.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class NaverLocalSearchResponseDto {
	private Channel rss;

	@Getter
	public static class Channel {
		private ChannelInner channel;

		@Getter
		public static class ChannelInner {
			private String lastBuildDate;
			private int total;
			private int start;
			private int display;
			private List<Item> item;

			@Getter
			public static class Item {
				private String title;
				private String link;
				private String category;
				private String description;
				private String telephone;
				private String address;
				private String roadAddress;
				private String mapx;
				private String mapy;
			}
		}
	}
}
