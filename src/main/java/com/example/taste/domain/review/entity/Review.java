package com.example.taste.domain.review.entity;

import com.example.taste.common.entity.BaseEntity;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
	private boolean isValidated;

	@Column(nullable = false)
	private boolean isPresented;

	@Column(nullable = false)
	private String contents;

	@Column(columnDefinition = "tinyint", nullable = false)
	private Integer score;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "image_id")
	private Image image;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Builder
	public Review(boolean isValidated, String contents, Integer score,
		Image image, User user, Store store) {
		this.contents = contents;
		this.score = score;
		this.image = image;
		this.user = user;
		this.store = store;
		this.setValidation(isValidated);
	}

	public void setValidation(Boolean validation) {
		this.isValidated = validation;
		this.isPresented = validation;
	}

	public void updateContents(String contents) {
		this.contents = contents;
	}

	public void updateScore(Integer score) {
		this.score = score;
	}

	public void updateImage(Image image) {
		this.image = image;
	}
}
