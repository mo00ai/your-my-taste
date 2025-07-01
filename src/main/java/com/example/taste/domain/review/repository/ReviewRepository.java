package com.example.taste.domain.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.review.entity.Review;
import com.example.taste.domain.store.entity.Store;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
	default List<Review> findTop3OrderByCreatedAtDesc(Store store) {
		return findTop3ByStoreAndImageIsNotNullOrderByCreatedAtDesc(store);
	}

	@EntityGraph(attributePaths = "image")
	List<Review> findTop3ByStoreAndImageIsNotNullOrderByCreatedAtDesc(Store store);
}
