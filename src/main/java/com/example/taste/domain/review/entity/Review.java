package com.example.taste.domain.review.entity;

import com.example.taste.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Review")
public class Review extends BaseEntity {
	@Id
	private Long id;

	@NotNull
	private boolean validated;

	@NotNull
	private String content;

	@NotNull
	@Column(columnDefinition = "tinyint")
	private int score;

	@Builder
	public Review(boolean validated, String content, int score) {
		this.validated = validated;
		this.content = content;
		this.score = score;
	}
}
