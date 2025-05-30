package com.example.taste.domain.notification.entity;

import com.example.taste.common.entity.BaseEntity;
import com.example.taste.domain.notification.NotificationCategory;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class NotificationInfo extends BaseEntity {
	@Id
	private Long id;

	@NotNull
	private NotificationCategory category;

	@NotNull
	private Boolean isRead = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_content_id", nullable = false)
	private NotificationContent notificationContent;

	@Builder
	public NotificationInfo(NotificationCategory category, Boolean isRead, NotificationContent notificationContent) {
		this.category = category;
		this.isRead = isRead != null ? isRead : false; // 초기값 세팅
		this.notificationContent = notificationContent;
	}
}
