package com.example.taste.domain.notification.entity;

import com.example.taste.common.entity.BaseEntity;
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
import jakarta.persistence.SequenceGenerator;
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "noti_seq")
	@SequenceGenerator(name = "noti_seq", sequenceName = "noti_seq", allocationSize = 100)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_target", nullable = false)
	private User user;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private NotificationCategory category;

	@Column(nullable = false)
	private Boolean isRead = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_content_id", nullable = false)
	private NotificationContent notificationContent;

	@Builder
	public NotificationInfo(NotificationCategory category, NotificationContent notificationContent,
		User user) {
		this.category = category;
		this.user = user;
		this.isRead = false;
		this.notificationContent = notificationContent;
	}

	public void readIt() {
		this.isRead = true;
	}
}
