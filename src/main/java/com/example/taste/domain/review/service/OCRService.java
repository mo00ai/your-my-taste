package com.example.taste.domain.review.service;

import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.review.dto.OcrRequestDto;
import com.example.taste.domain.review.dto.OcrResponseDto;
import com.example.taste.domain.review.exception.ReviewErrorCode;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.exception.StoreErrorCode;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OCRService {
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final RedisService redisService;
	private final WebClient webClient;

	@Value("${ocr.key}")
	private String secretKey;
	@Value("${ocr.host}")
	private String ocrHost;
	@Value("${ocr.path}")
	private String ocrPath;

	// 영수증 인증 생성
	public void createValidation(Long storeId, MultipartFile image, Long userId) throws
		IOException {

		// api 요청 보내기 전에 유저, 가게부터 확인
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

		if (image == null) {
			throw new CustomException(ReviewErrorCode.NO_IMAGE_REQUESTED);
		}

		String originalFilename = image.getOriginalFilename();
		if (originalFilename == null || !originalFilename.contains(".")) {
			throw new CustomException(ReviewErrorCode.BAD_OCR_IMAGE);
		}
		int dotindex = originalFilename.indexOf('.');
		if (dotindex == -1 || dotindex == originalFilename.length() - 1) {
			throw new CustomException(ReviewErrorCode.BAD_OCR_IMAGE);
		}
		String imageFormat = originalFilename.substring(dotindex + 1).toLowerCase();
		if (imageFormat.isBlank()) {
			throw new CustomException(ReviewErrorCode.BAD_OCR_IMAGE);
		}

		// 이미지는 인코딩해서 api에 전달
		String base64Image = Base64.getEncoder().encodeToString(image.getBytes());

		// 프로퍼티로 뺄 것.
		URI uri = UriComponentsBuilder.newInstance()
			.scheme("https")
			.host(ocrHost)
			.path(ocrPath)
			.build()
			.toUri();

		OcrRequestDto ocrRequestDto = OcrRequestDto.builder()
			.version("V2")
			.requestId(UUID.randomUUID().toString())
			.timestamp(System.currentTimeMillis())
			.images(List.of(OcrRequestDto.Images.builder()
				.format(imageFormat)
				.data(base64Image)
				.name("receipt_data")
				.build()))
			.build();

		Mono<OcrResponseDto> response = webClient
			.post()
			.uri(uri)
			.header("X-OCR-SECRET", secretKey)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(ocrRequestDto)
			.retrieve()
			.onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
				r -> r.bodyToMono(String.class)
					.flatMap(e -> Mono.error(new CustomException(ReviewErrorCode.OCR_CALL_FAILED))))
			.bodyToMono(OcrResponseDto.class);

		OcrResponseDto ocrResponseDto;

		try {
			ocrResponseDto = response.block(Duration.ofSeconds(10));
		} catch (Exception e) {
			throw new CustomException(ReviewErrorCode.OCR_CALL_FAILED, e.toString());
		}

		// 가게 이름 (ex: 프랭크 버거)
		String storeName = ocrResponseDto.getStoreName();
		if (storeName == null || storeName.isEmpty()) {
			throw new CustomException(ReviewErrorCode.BAD_OCR_RESPONSE);
		}

		// 가게 이름 상세 (ex: 성수점, 덕명점 1호 어쩌구...)
		String storeSubName = ocrResponseDto.getStoreSubName();

		StringBuilder fullName = new StringBuilder().append(storeName);
		if (storeSubName != null && !storeSubName.isBlank()) {
			fullName.append(" ").append(storeSubName);
		}

		// TODO 가게 이름 저장하는 방식에 맞춰 수정할 것.
		Boolean ocrResult = store.getName().contentEquals(fullName);

		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		String key = "reviewValidation:user:" + user.getId() + ":store:" + store.getId();
		redisService.setKeyValue(key, ocrResult, Duration.ofMinutes(5));
	}
}
