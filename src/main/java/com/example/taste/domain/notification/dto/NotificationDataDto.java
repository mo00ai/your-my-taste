package com.example.taste.domain.notification.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonSerialize
public class NotificationDataDto implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private User user;
	private NotificationCategory category;
	private String contents;
	private String redirectionUrl;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;
	private boolean read;

	@Builder
	public NotificationDataDto(User user, NotificationCategory category, String contents, String redirectionUrl,
		LocalDateTime createdAt, boolean read) {
		this.user = user;
		this.category = category;
		this.contents = contents;
		this.redirectionUrl = redirectionUrl;
		this.createdAt = createdAt;
		this.read = read;
	}

	public void readIt() {
		this.read = true;
	}

}
