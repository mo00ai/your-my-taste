package com.example.taste.domain.notification.entity;

import com.example.taste.common.entity.BaseEntity;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserNotificationDenySetting extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private User user;

	private NotificationCategory notificationCategory;

	@Builder
	public UserNotificationDenySetting(User user, NotificationCategory notificationCategory) {
		this.user = user;
		this.notificationCategory = notificationCategory;
	}
}
