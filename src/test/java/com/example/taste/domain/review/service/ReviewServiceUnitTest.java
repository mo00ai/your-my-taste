package com.example.taste.domain.review.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.image.service.ImageService;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.service.PkService;
import com.example.taste.domain.review.dto.CreateReviewRequestDto;
import com.example.taste.domain.review.dto.CreateReviewResponseDto;
import com.example.taste.domain.review.dto.GetReviewResponseDto;
import com.example.taste.domain.review.dto.UpdateReviewRequestDto;
import com.example.taste.domain.review.dto.UpdateReviewResponseDto;
import com.example.taste.domain.review.entity.Review;
import com.example.taste.domain.review.repository.ReviewRepository;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.CategoryFixture;
import com.example.taste.fixtures.StoreFixture;

@ExtendWith(MockitoExtension.class)
class ReviewServiceUnitTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private StoreRepository storeRepository;
	@Mock
	private ReviewRepository reviewRepository;
	@Mock
	private RedisService redisService;
	@Mock
	private ImageService imageService;
	@Mock
	private PkService pkService;

	@InjectMocks
	private ReviewService reviewService;

	@Nested
	class create {
		@Test
		void createReview() throws IOException {
			// given
			Long userId = 1L;
			Long storeId = 100L;
			MultipartFile file = mock(MultipartFile.class);
			List<MultipartFile> files = List.of(file);

			User user = spy(User.builder().build());
			Store store = Store.builder().build();
			Image image = Image.builder()
				.url("testUrl")
				.build();

			CreateReviewRequestDto requestDto = new CreateReviewRequestDto("맛있어요", 5);
			Review review = Review.builder()
				.user(user).store(store).image(image)
				.score(5).contents("맛있어요").isValidated(true)
				.build();

			given(imageService.saveImage(any(), any())).willReturn(image);
			given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
			given(user.getId()).willReturn(1L);
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(redisService.getKeyValue(anyString())).willReturn("true");
			given(reviewRepository.save(any())).willReturn(review);

			// when
			CreateReviewResponseDto result = reviewService.createReview(requestDto, storeId, files, ImageType.REVIEW,
				userId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getImageUrl()).isEqualTo("testUrl");
			assertThat(result.getContents()).isEqualTo("맛있어요");
			assertThat(result.getScore()).isEqualTo(5);
			then(pkService).should().savePkLog(userId, PkType.REVIEW);
		}

		@Test
		void no_user_found() throws IOException {
			// given
			Long userId = 1L;
			Long storeId = 100L;
			MultipartFile file = mock(MultipartFile.class);
			List<MultipartFile> files = List.of(file);

			Store store = Store.builder().build();
			Image image = Image.builder().build();

			CreateReviewRequestDto requestDto = new CreateReviewRequestDto("리뷰", 5);

			given(imageService.saveImage(any(), any())).willReturn(image);
			given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

			// when & then
			assertThatThrownBy(() -> reviewService.createReview(requestDto, storeId, files, ImageType.REVIEW,
				userId)).isInstanceOf(CustomException.class).hasMessageContaining("해당 유저를 찾을 수 없습니다.");
		}

		@Test
		void no_files_requested() throws IOException {
			// given
			Long userId = 1L;
			Long storeId = 100L;

			User user = spy(User.builder().build());
			Store store = Store.builder().build();

			CreateReviewRequestDto requestDto = new CreateReviewRequestDto("맛있어요", 5);
			Review review = Review.builder()
				.user(user).store(store)
				.score(5).contents("맛있어요").isValidated(true)
				.build();

			given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
			given(user.getId()).willReturn(1L);
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(redisService.getKeyValue(anyString())).willReturn("true");
			given(reviewRepository.save(any())).willReturn(review);

			// when
			CreateReviewResponseDto result = reviewService.createReview(requestDto, storeId, null, ImageType.REVIEW,
				userId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getImageUrl()).isEqualTo(null);
			assertThat(result.getContents()).isEqualTo("맛있어요");
			assertThat(result.getScore()).isEqualTo(5);
			then(imageService).shouldHaveNoInteractions();
			then(pkService).should().savePkLog(userId, PkType.REVIEW);
		}

		@Test
		void empty_files_requested() throws IOException {
			// given
			Long userId = 1L;
			Long storeId = 100L;

			User user = spy(User.builder().build());
			Store store = Store.builder().build();

			CreateReviewRequestDto requestDto = new CreateReviewRequestDto("맛있어요", 5);
			Review review = Review.builder()
				.user(user).store(store)
				.score(5).contents("맛있어요").isValidated(true)
				.build();

			given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
			given(user.getId()).willReturn(1L);
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(redisService.getKeyValue(anyString())).willReturn("true");
			given(reviewRepository.save(any())).willReturn(review);

			// when
			CreateReviewResponseDto result = reviewService.createReview(requestDto, storeId, Collections.EMPTY_LIST,
				ImageType.REVIEW,
				userId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getImageUrl()).isEqualTo(null);
			assertThat(result.getContents()).isEqualTo("맛있어요");
			assertThat(result.getScore()).isEqualTo(5);
			then(imageService).shouldHaveNoInteractions();
			then(pkService).should().savePkLog(userId, PkType.REVIEW);
		}
	}

	@Nested
	class update {
		@Test
		void updateReview() throws IOException {
			// given
			Long userId = 1L;
			Long reviewId = 10L;
			User user = spy(User.builder().build());
			Image image = Image.builder()
				.url("testUrl")
				.build();
			Store store = Store.builder().build();
			Review review = spy(Review.builder()
				.user(user)
				.store(store)
				.contents("이전 내용")
				.score(3)
				.image(image)
				.build());

			MultipartFile file = mock(MultipartFile.class);
			List<MultipartFile> files = List.of(file);
			UpdateReviewRequestDto requestDto = new UpdateReviewRequestDto("수정된 내용", 4);

			given(review.getId()).willReturn(1L);
			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
			given(user.getId()).willReturn(1L);
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(user.isSameUser(anyLong())).willReturn(true);
			given(redisService.getKeyValue(anyString())).willReturn("true");

			// when
			UpdateReviewResponseDto result = reviewService.updateReview(requestDto, reviewId, files, ImageType.REVIEW,
				userId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getContents()).isEqualTo("수정된 내용");
			assertThat(result.getScore()).isEqualTo(4);
			assertThat(result.getImageUrl()).isEqualTo("testUrl");
			then(imageService).should().update(image.getId(), ImageType.REVIEW, file);
		}

		@Test
		void review_had_no_image() throws IOException {
			// given
			Long userId = 1L;
			Long reviewId = 10L;
			User user = spy(User.builder().build());
			Store store = Store.builder().build();
			Review review = spy(Review.builder()
				.user(user)
				.store(store)
				.contents("이전 내용")
				.image(null)
				.score(3)
				.build());

			MultipartFile file = mock(MultipartFile.class);
			List<MultipartFile> files = List.of(file);
			UpdateReviewRequestDto requestDto = new UpdateReviewRequestDto("수정된 내용", 4);

			given(review.getId()).willReturn(1L);
			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
			given(user.getId()).willReturn(1L);
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(user.isSameUser(anyLong())).willReturn(true);
			given(redisService.getKeyValue(anyString())).willReturn("true");

			// when
			UpdateReviewResponseDto result = reviewService.updateReview(requestDto, reviewId, files, ImageType.REVIEW,
				userId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getContents()).isEqualTo("수정된 내용");
			assertThat(result.getScore()).isEqualTo(4);
			then(imageService).should().saveImage(files.get(0), ImageType.REVIEW);
		}

		@Test
		void no_files_requested() throws IOException {
			// given
			Long userId = 1L;
			Long reviewId = 10L;
			User user = spy(User.builder().build());
			Store store = Store.builder().build();
			Review review = spy(Review.builder()
				.user(user)
				.store(store)
				.contents("이전 내용")
				.image(null)
				.score(3)
				.build());

			UpdateReviewRequestDto requestDto = new UpdateReviewRequestDto("수정된 내용", 4);

			given(review.getId()).willReturn(1L);
			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
			given(user.getId()).willReturn(1L);
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(user.isSameUser(anyLong())).willReturn(true);
			given(redisService.getKeyValue(anyString())).willReturn("true");

			// when
			UpdateReviewResponseDto result = reviewService.updateReview(requestDto, reviewId, Collections.EMPTY_LIST,
				ImageType.REVIEW,
				userId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getContents()).isEqualTo("수정된 내용");
			assertThat(result.getScore()).isEqualTo(4);
			assertThat(result.getImageUrl()).isEqualTo(null);
			then(imageService).shouldHaveNoInteractions();
		}

		@Test
		void empty_files_requested() throws IOException {
			// given
			Long userId = 1L;
			Long reviewId = 10L;
			User user = spy(User.builder().build());
			Store store = Store.builder().build();
			Review review = spy(Review.builder()
				.user(user)
				.store(store)
				.contents("이전 내용")
				.image(null)
				.score(3)
				.build());

			UpdateReviewRequestDto requestDto = new UpdateReviewRequestDto("수정된 내용", 4);

			given(review.getId()).willReturn(1L);
			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
			given(user.getId()).willReturn(1L);
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(user.isSameUser(anyLong())).willReturn(true);
			given(redisService.getKeyValue(anyString())).willReturn("true");

			// when
			UpdateReviewResponseDto result = reviewService.updateReview(requestDto, reviewId, null, ImageType.REVIEW,
				userId);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getContents()).isEqualTo("수정된 내용");
			assertThat(result.getScore()).isEqualTo(4);
			assertThat(result.getImageUrl()).isEqualTo(null);
			then(imageService).shouldHaveNoInteractions();
		}

		@Test
		void wrong_user() throws IOException {
			// given
			Long userId = 1L;
			Long reviewId = 10L;
			User user = spy(User.builder().build());
			Store store = Store.builder().build();
			Review review = spy(Review.builder()
				.user(user)
				.store(store)
				.contents("이전 내용")
				.image(null)
				.score(3)
				.build());

			UpdateReviewRequestDto requestDto = new UpdateReviewRequestDto("수정된 내용", null);

			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
			given(user.getId()).willReturn(1L);
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(user.isSameUser(anyLong())).willReturn(false);

			// when
			assertThatThrownBy(() -> reviewService.updateReview(requestDto, reviewId, null, ImageType.REVIEW,
				userId)).isInstanceOf(CustomException.class).hasMessageContaining("");

		}
	}

	@Test
	void getAllReview() {
		// given
		Long storeId = 1L;
		int index = 1;
		int score = 0;

		User user = spy(User.builder().build());
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);
		ReflectionTestUtils.setField(store, "id", storeId);
		Review review = Review.builder()
			.score(3)
			.user(user)
			.store(store)
			.build();
		Page<Review> reviewPage = new PageImpl<>(List.of(review));

		given(user.getId()).willReturn(1L);
		given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
		given(reviewRepository.getAllReview(eq(storeId), any(Pageable.class), eq(score)))
			.willReturn(reviewPage);

		// when
		Page<GetReviewResponseDto> result = reviewService.getAllReview(storeId, index, score);

		// then
		assertThat(result.getTotalElements()).isEqualTo(1);
		GetReviewResponseDto dto = result.getContent().get(0);
		assertThat(dto.getUserId()).isEqualTo(1L);
		assertThat(dto.getScore()).isEqualTo(3);
	}

	@Test
	void getReview() {
		// given
		Long reviewId = 10L;
		User user = spy(User.builder().build());
		Review review = spy(Review.builder()
			.score(3)
			.user(user)
			.build());

		given(review.getId()).willReturn(reviewId);
		given(user.getId()).willReturn(1L);
		given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

		// when
		GetReviewResponseDto result = reviewService.getReview(reviewId);

		// then
		assertThat(result.getUserId()).isEqualTo(1L);
		assertThat(result.getScore()).isEqualTo(3);
		assertThat(result.getId()).isEqualTo(reviewId);
	}

	@Nested
	class delete {
		@Test
		void deleteReview() {
			// given
			Long reviewId = 10L;
			Long userId = 1L;

			User user = spy(User.builder().build());
			Review review = spy(Review.builder().user(user).build());

			given(user.getId()).willReturn(userId);
			given(user.isSameUser(userId)).willReturn(true);
			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
			given(userRepository.findById(userId)).willReturn(Optional.of(user));

			// when
			reviewService.deleteReview(reviewId, userId);

			// then
			then(reviewRepository).should().delete(review);
		}

		@Test
		void wrong_user() {
			// given
			Long reviewId = 10L;
			Long userId = 1L;

			User user = spy(User.builder().build());
			Review review = spy(Review.builder().user(user).build());

			given(user.getId()).willReturn(userId);
			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
			given(userRepository.findById(userId)).willReturn(Optional.of(user));

			// when
			assertThatThrownBy(() -> reviewService.deleteReview(reviewId, userId))
				.isInstanceOf(CustomException.class).hasMessageContaining("본인이 작성한 리뷰가 아닙니다.");
		}
	}

}
