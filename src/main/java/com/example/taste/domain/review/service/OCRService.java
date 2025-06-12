package com.example.taste.domain.review.service;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.review.dto.OcrRequestDto;
import com.example.taste.domain.review.dto.OcrResponseDto;
import com.example.taste.domain.review.exception.ReviewErrorCode;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OCRService {

	private final EntityFetcher entityFetcher;
	private final RedisService redisService;

	@Value("${ocr_key}")
	private String secretKey;

	// 영수증 인증 생성
	public void createValidation(Long storeId, MultipartFile image, CustomUserDetails userDetails) throws
		IOException {

		// api 요청 보내기 전에 유저, 가게부터 확인
		User user = entityFetcher.getUserOrThrow(userDetails.getId());
		Store store = entityFetcher.getStoreOrThrow(storeId);

		if (image == null) {
			throw new CustomException(ReviewErrorCode.NO_IMAGE_REQUESTED);
		}

		// 이미지는 인코딩해서 api에 전달
		String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
		// TODO url 리팩토링
		UriComponents uriComponents = UriComponentsBuilder.fromUriString("").build();
		String apiUrl = "https://dwyd1vrxhu.apigw.ntruss.com/custom/v1/42612/f7152a2fe3e8899aaf3d099f7c46439dfd24c7a5c926edaed8d9a471ae0b563e/document/receipt";

		//TODO webclient-> webflux
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-OCR-SECRET", secretKey);

		OcrRequestDto ocrRequestDto = OcrRequestDto.builder()
			.version("V2")
			.requestId(UUID.randomUUID().toString())
			.timestamp(System.currentTimeMillis())
			.images(List.of(OcrRequestDto.Images.builder()
				.format("png")
				.data(base64Image)
				.name("receipt_data")
				.build()))
			.build();

		HttpEntity<OcrRequestDto> entity = new HttpEntity<>(ocrRequestDto, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

		// 성공 응답 아니면 예외
		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new CustomException(ReviewErrorCode.OCR_CALL_FAILED);
		}

		// 응답 맵핑해서 사용
		ObjectMapper objectMapper = new ObjectMapper();
		OcrResponseDto ocrResponsedto = objectMapper.readValue(response.getBody(), OcrResponseDto.class);

		// 가게 이름 (ex: 프랭크 버거)
		String storeName = Optional.ofNullable(ocrResponsedto.getImages()).filter(images -> !images.isEmpty())
			.map(images -> images.get(0))
			.map(i -> i.getReceipt())
			.map(receipt -> receipt.getResult())
			.map(result -> result.getStoreInfo())
			.map(storeInfo -> storeInfo.getName())
			.map(name -> name.getText())
			.orElseThrow(() -> new CustomException(ReviewErrorCode.STORE_NAME_NOT_FOUND));

		// 가게 이름 상세 (ex: 성수점, 덕명점 1호 어쩌구...)
		String storeSubName = Optional.ofNullable(ocrResponsedto.getImages())
			.filter(images -> !images.isEmpty())
			.map(images -> images.get(0))
			.map(i -> i.getReceipt())
			.map(receipt -> receipt.getResult())
			.map(result -> result.getStoreInfo())
			.map(storeInfo -> storeInfo.getSubName())
			.map(subName -> subName.getText())
			.orElse(null);  // 없으면 null 반환

		// TODO 가게 이름 저장하는 방식에 맞춰 수정할 것.
		Boolean ocrResult = store.getName().equals(storeName);
		String key = "reviewValidation:user:" + user.getId() + ":store:" + store.getId();
		redisService.setKeyValue(key, ocrResult, Duration.ofMinutes(5));
	}
}
