package com.example.taste.domain.review.entity;

import com.example.taste.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Review")
@AllArgsConstructor
public class Review extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private boolean validated;

	@Column(nullable = false)
	private boolean isPresented;

	@Column(nullable = false)
	private String content;

	@Column(columnDefinition = "tinyint", nullable = false)
	private int score;

	@Builder
	public Review(boolean validated, String content, int score, boolean isPresented) {
		this.validated = validated;
		this.content = content;
		this.score = score;
		this.isPresented = isPresented;
	}
}
