package com.example.taste.domain.notification.entity;

import com.example.taste.common.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class NotificationContent extends BaseEntity {
	@Id
	private Long id;

	@NotNull
	private String content;

	private String redirectionUrl;

	@Builder
	public NotificationContent(String content, String redirectionUrl) {
		this.content = content;
		this.redirectionUrl = redirectionUrl;
	}
}
