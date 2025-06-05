package com.example.taste.domain.store.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.example.taste.common.entity.BaseEntity;
import com.example.taste.domain.review.entity.Review;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Store extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@Column(nullable = false)
	private String name;

	private String description;

	private String address;

	private String roadAddress;

	@Column(nullable = false)
	private BigDecimal mapx;

	@Column(nullable = false)
	private BigDecimal mapy;

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Review> reviewList;

	@Builder
	public Store(Category category, String name, String description, String address, String roadAddress,
		BigDecimal mapx, BigDecimal mapy) {

		this.category = category;
		this.name = name;
		this.description = description;
		this.address = address;
		this.roadAddress = roadAddress;
		this.mapx = mapx;
		this.mapy = mapy;
	}

	public void addReview(Review review) {
		if (this.reviewList == null) {
			this.reviewList = new ArrayList<>();
		}
		this.reviewList.add(review);
	}

	public void removeReview(Review review) {
		this.reviewList.remove(review);
	}
}
