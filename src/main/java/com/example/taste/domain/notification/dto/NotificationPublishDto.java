package com.example.taste.domain.notification.dto;

import java.io.Serial;
import java.io.Serializable;

import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.notification.entity.enums.NotificationType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@JsonSerialize
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPublishDto implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long userId;
	@NotNull(message = "올바르지 않은 알림 생성 요청입니다.")
	private NotificationCategory category;
	@NotNull(message = "올바르지 않은 알림 생성 요청입니다.")
	private NotificationType type;
	private String redirectionUrl;
	private Long redirectionEntityId;
	private String additionalText;
}
