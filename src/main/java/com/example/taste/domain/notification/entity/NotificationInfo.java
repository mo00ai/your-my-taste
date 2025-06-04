package com.example.taste.domain.notification.entity;

import com.example.taste.common.entity.BaseEntity;
import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class NotificationInfo extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_target", nullable = false)
	private User user;

	@Column(nullable = false)
	private NotificationCategory category;

	@Column(nullable = false)
	private Boolean isRead = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_content_id", nullable = false)
	@Enumerated(EnumType.STRING)
	private NotificationContent notificationContent;

	@Builder
	public NotificationInfo(NotificationCategory category, Boolean isRead, NotificationContent notificationContent,
		User user) {
		this.category = category;
		this.user = user;
		this.isRead = isRead != null ? isRead : false; // 초기값 세팅
		this.notificationContent = notificationContent;
	}
}
