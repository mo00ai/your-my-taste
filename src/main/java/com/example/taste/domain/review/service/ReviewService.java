package com.example.taste.domain.review.service;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.image.service.ImageService;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.service.PkService;
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
import com.example.taste.domain.store.service.StoreService;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final RedisService redisService;
	private final ImageService imageService;
	private final StoreService storeService;
	private final UserService userService;
	private final PkService pkService;

	@Value("${ocr_key}")
	private String secretKey;

	@Transactional
	public CreateReviewResponseDto createReview(CreateReviewRequestDto requestDto, Long storeId,
		List<MultipartFile> files, ImageType imageType, CustomUserDetails userDetails) throws IOException {
		Image image = null;
		if (files != null && !files.isEmpty()) {
			image = imageService.saveImage(files.get(0), imageType);
		}
		Store store = storeService.findById(storeId);
		User user = userService.findById(userDetails.getId());

		String key = "reviewValidation:user:" + user.getId() + ":store:" + store.getId();
		Object value = redisService.getKeyValue(key);
		Boolean valid = value != null ? (Boolean)value : false;

		Review review = Review.builder()
			.contents(requestDto.getContents())
			.image(image)
			.store(store)
			.score(requestDto.getScore())
			.user(user)
			.isValidated(valid)
			.build();

		Review saved = reviewRepository.save(review);
		pkService.savePkLog(user.getId(), PkType.REVIEW);
		return new CreateReviewResponseDto(saved);
	}

	@Transactional
	public UpdateReviewResponseDto updateReview(UpdateReviewRequestDto requestDto, Long reviewId,
		MultipartFile multipartFile, ImageType imageType, CustomUserDetails userDetails) throws IOException {
		Review review = findById(reviewId);
		User user = userService.findById(userDetails.getId());
		if (!review.getUser().equals(user)) {
			throw new CustomException(ReviewErrorCode.REVIEW_USER_MISMATCH);
		}

		String contents =
			requestDto.getContents().isEmpty() ? review.getContents() :
				requestDto.getContents();
		Image image = multipartFile == null ? review.getImage() : imageService.saveImage(multipartFile, imageType);
		Integer score = requestDto.getScore() == null ? review.getScore() : requestDto.getScore();

		String key = "reviewValidation:user:" + review.getUser().getId() + ":store:" + review.getStore().getId();
		Object value = redisService.getKeyValue(key);
		Boolean valid = value != null ? (Boolean)value : false;

		review.updateContents(contents);
		review.updateScore(score);
		review.updateImage(image);
		review.setValidation(valid);
		return new UpdateReviewResponseDto(review);
	}

	@Transactional(readOnly = true)
	public Page<GetReviewResponseDto> getAllReview(Long storeId, int index, int score) {
		Store store = storeService.findById(storeId);
		Pageable pageable = PageRequest.of(index - 1, 10);
		Page<Review> reviews = reviewRepository.getAllReview(store, pageable, score);
		return reviews.map(GetReviewResponseDto::new);
	}

	@Transactional(readOnly = true)
	public GetReviewResponseDto getReview(Long reviewId) {
		return new GetReviewResponseDto(findById(reviewId));
	}

	@Transactional
	public void deleteReview(Long reviewId, CustomUserDetails userDetails) {
		Review review = findById(reviewId);
		User user = userService.findById(userDetails.getId());
		if (!review.getUser().equals(user)) {
			throw new CustomException(ReviewErrorCode.REVIEW_USER_MISMATCH);
		}

		review.getStore().removeReview(review);
	}

	public void createValidation(Long storeId, MultipartFile image, CustomUserDetails userDetails) throws IOException {
		if (image == null) {
			throw new CustomException(ReviewErrorCode.NO_IMAGE_REQUESTED);
		}
		String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
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
			throw new CustomException(ReviewErrorCode.OCR_CALL_FAILED);
		}

		ObjectMapper objectMapper = new ObjectMapper();
		OcrResponseDto ocrResponsedto = objectMapper.readValue(response.getBody(), OcrResponseDto.class);

		String storeName = Optional.ofNullable(ocrResponsedto.getImages()).filter(images -> !images.isEmpty())
			.map(images -> images.get(0))
			.map(i -> i.getReceipt())
			.map(receipt -> receipt.getResult())
			.map(result -> result.getStoreInfo())
			.map(storeInfo -> storeInfo.getName())
			.map(name -> name.getText())
			.orElseThrow(() -> new CustomException(ReviewErrorCode.STORE_NAME_NOT_FOUND));

		String storeSubName = Optional.ofNullable(ocrResponsedto.getImages())
			.filter(images -> !images.isEmpty())
			.map(images -> images.get(0))
			.map(i -> i.getReceipt())
			.map(receipt -> receipt.getResult())
			.map(result -> result.getStoreInfo())
			.map(storeInfo -> storeInfo.getSubName())
			.map(subName -> subName.getText())
			.orElse(null);  // 없으면 null 반환

		User user = userService.findById(userDetails.getId());
		Store store = storeService.findById(storeId);

		// 가게 이름 어떻게 저장하나
		Boolean ocrResult = store.getName().equals(storeName);
		String key = "reviewValidation:user:" + user.getId() + ":store:" + store.getId();
		redisService.setKeyValue(key, ocrResult, Duration.ofMinutes(5));
	}

	@Transactional(readOnly = true)
	public Review findById(Long reviewId) {
		Review review = reviewRepository.getReviewWithUserAndStore(reviewId)
			.orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));
		return review;
	}
}
