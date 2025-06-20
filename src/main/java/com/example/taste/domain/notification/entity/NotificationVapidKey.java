package com.example.taste.domain.notification.entity;

import com.example.taste.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@EqualsAndHashCode
@NoArgsConstructor
public class NotificationVapidKey {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String endpoint;
	private String p256dhKey;
	private String authKey;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	public NotificationVapidKey(String authKey, String p256dhKey, String endpoint) {
		this.authKey = authKey;
		this.p256dhKey = p256dhKey;
		this.endpoint = endpoint;
	}
}
