package com.example.taste.domain.notification.entity;

import java.time.LocalDateTime;

import com.example.taste.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@EqualsAndHashCode
@NoArgsConstructor
@Getter
public class WebPushSubscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, length = 1000)
	private String endpoint;
	@Setter
	private String p256dhKey;
	@Setter
	private String authKey;

	private LocalDateTime expirationTime;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@Builder
	public WebPushSubscription(String authKey, String p256dhKey, String endpoint, User user) {
		this.authKey = authKey;
		this.p256dhKey = p256dhKey;
		this.endpoint = endpoint;
		this.user = user;
	}
}
