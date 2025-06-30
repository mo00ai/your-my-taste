package com.example.taste.domain.store.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.review.entity.Review;
import com.example.taste.domain.review.repository.ReviewRepository;
import com.example.taste.domain.store.dto.response.StoreResponse;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.entity.StoreBucketItem;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreBucketItemRepository;
import com.example.taste.domain.store.repository.StoreBucketRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.CategoryFixture;
import com.example.taste.fixtures.ImageFixture;
import com.example.taste.fixtures.ReviewFixture;
import com.example.taste.fixtures.StoreBucketFixture;
import com.example.taste.fixtures.StoreBucketItemFixture;
import com.example.taste.fixtures.StoreFixture;
import com.example.taste.fixtures.UserFixture;
import com.example.taste.property.AbstractIntegrationTest;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest
class StoreServiceTest extends AbstractIntegrationTest {
	@Autowired
	private StoreService storeService;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private StoreBucketRepository storeBucketRepository;
	@Autowired
	private StoreBucketItemRepository storeBucketItemRepository;
	@Autowired
	private ReviewRepository reviewRepository;
	@Autowired
	private EntityManager em;

	@Test
	@Transactional
	void getStore_whenResultInclude2Images_thenReturnImages() {
		// given
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.saveAndFlush(StoreFixture.create(category));

		Image image1 = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image1));

		Image image2 = ImageFixture.create();
		Review review1 = reviewRepository.save(ReviewFixture.create(image2, user, store));
		Image image3 = ImageFixture.create();
		Review review2 = reviewRepository.save(ReviewFixture.create(image3, user, store));

		// when
		StoreResponse dto = storeService.getStore(store.getId());

		// then
		assertThat(dto.getReviewImages().size()).isEqualTo(2);
	}

	@Test
	@Transactional
	void getStore_thenResultInclude4Images_thenReturnLatest3Reviews() {
		// given
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.saveAndFlush(StoreFixture.create(category));

		Image image1 = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image1));

		Image image2 = ImageFixture.create();
		Review review1 = reviewRepository.saveAndFlush(ReviewFixture.create(image2, user, store));
		Image image3 = ImageFixture.create();
		Review review2 = reviewRepository.save(ReviewFixture.create(image3, user, store));
		Image image4 = ImageFixture.create();
		Review review3 = reviewRepository.save(ReviewFixture.create(image4, user, store));
		Image image5 = ImageFixture.create();
		Review review4 = reviewRepository.save(ReviewFixture.create(image5, user, store));

		// when
		List<Review> reviews = reviewRepository.findTop3OrderByCreatedAtDesc(store);
		List<String> imageUrls = reviews.stream().map(r -> r.getImage().getUrl())
			.toList();

		// then
		assertThat(imageUrls.size()).isEqualTo(3);
		assertThat(reviews.stream().filter(r -> r.getId().equals(review1.getId())).toList()).isEmpty();
	}

	@Test
	@Transactional
	void deleteStore_cascadeDeleteBucketItem() {
		// given
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.saveAndFlush(StoreFixture.create(category));

		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));

		StoreBucket closedBucket = storeBucketRepository.save(StoreBucketFixture.createClosedBucket(user));
		StoreBucketItem item = storeBucketItemRepository.saveAndFlush(
			StoreBucketItemFixture.create(closedBucket, store));

		// when
		storeService.deleteStore(store.getId());

		// then
		assertThat(storeBucketItemRepository.existsById(item.getId())).isFalse();
	}
}