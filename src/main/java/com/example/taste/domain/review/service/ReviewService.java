package com.example.taste.domain.review.service;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.review.dto.CreateReviewRequestDto;
import com.example.taste.domain.review.dto.CreateReviewResponseDto;
import com.example.taste.domain.review.dto.GetReviewResponseDto;
import com.example.taste.domain.review.dto.OcrResponseDto;
import com.example.taste.domain.review.dto.UpdateReviewRequestDto;
import com.example.taste.domain.review.dto.UpdateReviewResponseDto;
import com.example.taste.domain.review.entity.Review;
import com.example.taste.domain.review.exception.ReviewErrorCode;
import com.example.taste.domain.review.repository.ReviewRepository;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final RedisTemplate<String, Boolean> redisTemplate;
	@Value("${ocr_key}")
	private String secretKey;

	public CreateReviewResponseDto createReview(CreateReviewRequestDto requestDto, Long storeId) {
		Image tempImage = Image.builder().build();
		Store tempStore = Store.builder().build();
		User tempUser = User.builder().build();
		Boolean tempValid = true;
		Review review = Review.builder()
			.contents(requestDto.getContents())
			.image(tempImage)
			.store(tempStore)
			.score(requestDto.getScore())
			.user(tempUser)
			.validated(tempValid)
			.build();
		Review saved = reviewRepository.save(review);
		return new CreateReviewResponseDto(saved);
	}

	@Transactional
	public UpdateReviewResponseDto updateReview(UpdateReviewRequestDto requestDto, Long reviewId) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));
		String contents = requestDto.getContents().isEmpty() ? review.getContents() : requestDto.getContents();
		Image tempImage = requestDto.getImageID() == null ? review.getImage() : Image.builder().build();
		Integer score = requestDto.getScore() == null ? review.getScore() : requestDto.getScore();
		Boolean tempValid = true;
		review.updateContents(contents);
		review.updateScore(score);
		review.updateImage(tempImage);
		review.setValidated(tempValid);
		return new UpdateReviewResponseDto(review);
	}

	public Page<GetReviewResponseDto> getAllReview(Long storeId, int index, int score) {
		Store tempStore = Store.builder().build();
		Pageable pageable = PageRequest.of(index - 1, 10);
		Page<Review> reviews = reviewRepository.getAllReview(tempStore, pageable, score);
		return reviews.map(GetReviewResponseDto::new);
	}

	public GetReviewResponseDto getReview(Long reviewId) {
		return new GetReviewResponseDto(reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND)));
	}

	public void deleteReview(Long reviewId) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));
		reviewRepository.delete(review);
	}

	public String createValidation(Long storeId, MultipartFile image) throws IOException {

		if (image == null) {
			throw new CustomException(ReviewErrorCode.NO_IMAGE_REQUESTED);
		}
		String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
		User tempUser = User.builder().build();
		Store tempStore = Store.builder().build();

		String apiUrl = "https://dwyd1vrxhu.apigw.ntruss.com/custom/v1/42612/f7152a2fe3e8899aaf3d099f7c46439dfd24c7a5c926edaed8d9a471ae0b563e/document/receipt";

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-OCR-SECRET", secretKey);

		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("version", "V2");
		requestMap.put("requestId", UUID.randomUUID().toString());
		requestMap.put("timestamp", System.currentTimeMillis());

		Map<String, Object> imageMap = new HashMap<>();
		imageMap.put("format", "png");
		imageMap.put("data", base64Image);
		imageMap.put("name", "receipt_data");

		requestMap.put("images", List.of(imageMap));

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new RuntimeException();
		}

		ObjectMapper objectMapper = new ObjectMapper();
		OcrResponseDto ocrResponsedto = objectMapper.readValue(response.getBody(), OcrResponseDto.class);

		String validationResult = ocrResponsedto.getImages().get(0).getValidationResult().getResult();
		String storeName = ocrResponsedto.getImages()
			.get(0)
			.getReceipt()
			.getResult()
			.getStoreInfo()
			.getName()
			.getText();
		String storeSubName = ocrResponsedto.getImages()
			.get(0)
			.getReceipt()
			.getResult()
			.getStoreInfo()
			.getSubName()
			.getText();

		Boolean ocrResult = tempStore.getName().equals(storeName) && validationResult.equals("VALID");
		String key = "reviewValidation:user:" + tempUser.getId() + ":store:" + tempStore.getId();
		redisTemplate.opsForValue().set(key, ocrResult, Duration.ofMinutes(10));
		return ocrResult ? "인증되었습니다." : "인증에 실패하였습니다.";
	}
}
