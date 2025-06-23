package com.example.taste.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PushSubscribeRequestDto {

	private String endPoint;

	private Keys keys;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Keys {
		private String p256dh;
		private String auth;
	}

}
