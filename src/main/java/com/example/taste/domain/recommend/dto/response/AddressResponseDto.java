package com.example.taste.domain.recommend.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // Jackson 역직렬화용
public class AddressResponseDto {

	private List<Document> documents;

	@Getter
	@NoArgsConstructor // Jackson 역직렬화용
	public static class Document {
		private String address_name;
		private String x; // 경도
		private String y; // 위도

		@Builder
		public Document(String address_name, String x, String y) {
			this.address_name = address_name;
			this.x = x;
			this.y = y;
		}
	}
}
