package com.example.taste.domain.comment.entity;

import java.time.LocalDateTime;

import com.example.taste.common.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Comment extends BaseEntity {
	@Id
	private Long id;

	@NotNull
	private String content;

	private LocalDateTime deletedAt;

	@Builder
	public Comment(String content) {
		this.content = content;
	}
}
