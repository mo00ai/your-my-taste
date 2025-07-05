package com.example.taste.domain.review.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.review.dto.OcrResponseDto;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class OCRServiceUnitTest {

	@InjectMocks
	private OCRService ocrService;

	@Mock
	private UserRepository userRepository;
	@Mock
	private StoreRepository storeRepository;
	@Mock
	private RedisService redisService;
	@Mock
	private WebClient webClient;
	@Mock
	private WebClient.RequestBodyUriSpec uriSpec;
	@Mock
	private WebClient.RequestBodySpec bodySpec;
	@Mock
	@SuppressWarnings("rawtypes")
	private WebClient.RequestHeadersSpec headersSpec;
	@Mock
	private WebClient.ResponseSpec responseSpec;

	@Test
	void createValidation() throws IOException {
		// given
		Long storeId = 1L;
		Long userId = 2L;

		Store store = spy(Store.builder().build());
		given(store.getId()).willReturn(storeId);
		given(store.getName()).willReturn("버거킹 강남점");

		User user = spy(User.builder().build());
		given(user.getId()).willReturn(userId);

		given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		MultipartFile image = mock(MultipartFile.class);
		byte[] imageBytes = "dummyImage".getBytes(StandardCharsets.UTF_8);
		given(image.getOriginalFilename()).willReturn("receipt.png");
		given(image.getBytes()).willReturn(imageBytes);

		// WebClient mock chain
		given(webClient.post()).willReturn(uriSpec);
		given(uriSpec.uri(any(URI.class))).willReturn(bodySpec);
		given(bodySpec.header(anyString(), anyString())).willReturn(bodySpec);
		given(bodySpec.contentType(any())).willReturn(bodySpec);
		given(bodySpec.bodyValue(any())).willReturn(headersSpec);
		given(headersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);

		// OCR 응답 DTO
		OcrResponseDto ocrResponseDto = makeOcrResponse("버거킹", "강남점");
		given(responseSpec.bodyToMono(OcrResponseDto.class)).willReturn(Mono.just(ocrResponseDto));

		// WebClient 설정 (환경값)
		ReflectionTestUtils.setField(ocrService, "secretKey", "test-key");
		ReflectionTestUtils.setField(ocrService, "ocrHost", "test.ocr.com");
		ReflectionTestUtils.setField(ocrService, "ocrPath", "/v1/test");

		// when
		ocrService.createValidation(storeId, image, userId);

		// then
		String expectedKey = "reviewValidation:user:2:store:1";
		then(redisService).should().setKeyValue(eq(expectedKey), eq(true), any(Duration.class));
	}

	@Test
	void no_store_subname() throws IOException {
		// given
		Long storeId = 1L;
		Long userId = 2L;

		Store store = spy(Store.builder().build());
		given(store.getId()).willReturn(storeId);
		given(store.getName()).willReturn("버거킹");

		User user = spy(User.builder().build());
		given(user.getId()).willReturn(userId);

		given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		MultipartFile image = mock(MultipartFile.class);
		byte[] imageBytes = "dummyImage".getBytes(StandardCharsets.UTF_8);
		given(image.getOriginalFilename()).willReturn("receipt.png");
		given(image.getBytes()).willReturn(imageBytes);

		// WebClient mock chain
		given(webClient.post()).willReturn(uriSpec);
		given(uriSpec.uri(any(URI.class))).willReturn(bodySpec);
		given(bodySpec.header(anyString(), anyString())).willReturn(bodySpec);
		given(bodySpec.contentType(any())).willReturn(bodySpec);
		given(bodySpec.bodyValue(any())).willReturn(headersSpec);
		given(headersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);

		// OCR 응답 DTO
		OcrResponseDto ocrResponseDto = makeOcrResponse("버거킹", null);
		given(responseSpec.bodyToMono(OcrResponseDto.class)).willReturn(Mono.just(ocrResponseDto));

		// WebClient 설정 (환경값)
		ReflectionTestUtils.setField(ocrService, "secretKey", "test-key");
		ReflectionTestUtils.setField(ocrService, "ocrHost", "test.ocr.com");
		ReflectionTestUtils.setField(ocrService, "ocrPath", "/v1/test");

		// when
		ocrService.createValidation(storeId, image, userId);

		// then
		String expectedKey = "reviewValidation:user:2:store:1";
		then(redisService).should().setKeyValue(eq(expectedKey), eq(true), any(Duration.class));
	}

	@Test
	void store_subname_empty() throws IOException {
		// given
		Long storeId = 1L;
		Long userId = 2L;

		Store store = spy(Store.builder().build());
		given(store.getId()).willReturn(storeId);
		given(store.getName()).willReturn("버거킹");

		User user = spy(User.builder().build());
		given(user.getId()).willReturn(userId);

		given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		MultipartFile image = mock(MultipartFile.class);
		byte[] imageBytes = "dummyImage".getBytes(StandardCharsets.UTF_8);
		given(image.getOriginalFilename()).willReturn("receipt.png");
		given(image.getBytes()).willReturn(imageBytes);

		// WebClient mock chain
		given(webClient.post()).willReturn(uriSpec);
		given(uriSpec.uri(any(URI.class))).willReturn(bodySpec);
		given(bodySpec.header(anyString(), anyString())).willReturn(bodySpec);
		given(bodySpec.contentType(any())).willReturn(bodySpec);
		given(bodySpec.bodyValue(any())).willReturn(headersSpec);
		given(headersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);

		// OCR 응답 DTO
		OcrResponseDto ocrResponseDto = makeOcrResponse("버거킹", "");
		given(responseSpec.bodyToMono(OcrResponseDto.class)).willReturn(Mono.just(ocrResponseDto));

		// WebClient 설정 (환경값)
		ReflectionTestUtils.setField(ocrService, "secretKey", "test-key");
		ReflectionTestUtils.setField(ocrService, "ocrHost", "test.ocr.com");
		ReflectionTestUtils.setField(ocrService, "ocrPath", "/v1/test");

		// when
		ocrService.createValidation(storeId, image, userId);

		// then
		String expectedKey = "reviewValidation:user:2:store:1";
		then(redisService).should().setKeyValue(eq(expectedKey), eq(true), any(Duration.class));
	}

	@Test
	void store_name_empty() throws IOException {
		// given
		Long storeId = 1L;
		Long userId = 2L;

		Store store = spy(Store.builder().build());

		User user = spy(User.builder().build());

		given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

		MultipartFile image = mock(MultipartFile.class);
		byte[] imageBytes = "dummyImage".getBytes(StandardCharsets.UTF_8);
		given(image.getOriginalFilename()).willReturn("receipt.png");
		given(image.getBytes()).willReturn(imageBytes);

		// WebClient mock chain
		given(webClient.post()).willReturn(uriSpec);
		given(uriSpec.uri(any(URI.class))).willReturn(bodySpec);
		given(bodySpec.header(anyString(), anyString())).willReturn(bodySpec);
		given(bodySpec.contentType(any())).willReturn(bodySpec);
		given(bodySpec.bodyValue(any())).willReturn(headersSpec);
		given(headersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);

		// OCR 응답 DTO
		OcrResponseDto ocrResponseDto = makeOcrResponse("", "");
		given(responseSpec.bodyToMono(OcrResponseDto.class)).willReturn(Mono.just(ocrResponseDto));

		// WebClient 설정 (환경값)
		ReflectionTestUtils.setField(ocrService, "secretKey", "test-key");
		ReflectionTestUtils.setField(ocrService, "ocrHost", "test.ocr.com");
		ReflectionTestUtils.setField(ocrService, "ocrPath", "/v1/test");

		// when & then
		assertThatThrownBy(() -> ocrService.createValidation(storeId, image, userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("OCR 응답이 올바르지 않습니다.");

	}

	@Test
	void store_name_null() throws IOException {
		// given
		Long storeId = 1L;
		Long userId = 2L;

		Store store = spy(Store.builder().build());

		User user = spy(User.builder().build());

		given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

		MultipartFile image = mock(MultipartFile.class);
		byte[] imageBytes = "dummyImage".getBytes(StandardCharsets.UTF_8);
		given(image.getOriginalFilename()).willReturn("receipt.png");
		given(image.getBytes()).willReturn(imageBytes);
		// WebClient mock chain
		given(webClient.post()).willReturn(uriSpec);
		given(uriSpec.uri(any(URI.class))).willReturn(bodySpec);
		given(bodySpec.header(anyString(), anyString())).willReturn(bodySpec);
		given(bodySpec.contentType(any())).willReturn(bodySpec);
		given(bodySpec.bodyValue(any())).willReturn(headersSpec);
		given(headersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);

		// OCR 응답 DTO
		OcrResponseDto ocrResponseDto = makeOcrResponse(null, "");
		given(responseSpec.bodyToMono(OcrResponseDto.class)).willReturn(Mono.just(ocrResponseDto));

		// WebClient 설정 (환경값)
		ReflectionTestUtils.setField(ocrService, "secretKey", "test-key");
		ReflectionTestUtils.setField(ocrService, "ocrHost", "test.ocr.com");
		ReflectionTestUtils.setField(ocrService, "ocrPath", "/v1/test");

		// when & then
		assertThatThrownBy(() -> ocrService.createValidation(storeId, image, userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("OCR 응답이 올바르지 않습니다.");

	}

	@Test
	void mono_throw_exception() throws IOException {
		// given
		Long storeId = 1L;
		Long userId = 2L;

		Store store = spy(Store.builder().build());

		User user = spy(User.builder().build());

		given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

		MultipartFile image = mock(MultipartFile.class);
		byte[] imageBytes = "dummyImage".getBytes(StandardCharsets.UTF_8);
		given(image.getOriginalFilename()).willReturn("receipt.png");
		given(image.getBytes()).willReturn(imageBytes);

		// WebClient mock chain
		given(webClient.post()).willReturn(uriSpec);
		given(uriSpec.uri(any(URI.class))).willReturn(bodySpec);
		given(bodySpec.header(anyString(), anyString())).willReturn(bodySpec);
		given(bodySpec.contentType(any())).willReturn(bodySpec);
		given(bodySpec.bodyValue(any())).willReturn(headersSpec);
		given(headersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);

		// OCR 응답 DTO
		OcrResponseDto ocrResponseDto = makeOcrResponse(null, "");
		given(responseSpec.bodyToMono(OcrResponseDto.class)).willReturn(Mono.error(new RuntimeException()));

		// WebClient 설정 (환경값)
		ReflectionTestUtils.setField(ocrService, "secretKey", "test-key");
		ReflectionTestUtils.setField(ocrService, "ocrHost", "test.ocr.com");
		ReflectionTestUtils.setField(ocrService, "ocrPath", "/v1/test");

		// when & then
		assertThatThrownBy(() -> ocrService.createValidation(storeId, image, userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("java.lang.RuntimeException");
	}

	@Test
	void bad_image_format() throws IOException {
		// given
		Long storeId = 1L;
		Long userId = 2L;

		Store store = spy(Store.builder().build());

		given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

		MultipartFile image = mock(MultipartFile.class);
		given(image.getOriginalFilename()).willReturn("receipt.  ");

		// when & then
		assertThatThrownBy(() -> ocrService.createValidation(storeId, image, userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("OCR 요청 이미지가 올바르지 않습니다.");
	}

	@Test
	void no_image_format() throws IOException {
		// given
		Long storeId = 1L;
		Long userId = 2L;

		Store store = spy(Store.builder().build());

		given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

		MultipartFile image = mock(MultipartFile.class);
		given(image.getOriginalFilename()).willReturn("receipt.");

		// when & then
		assertThatThrownBy(() -> ocrService.createValidation(storeId, image, userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("OCR 요청 이미지가 올바르지 않습니다.");
	}

	@Test
	void blank_extension_should() throws IOException {
		MultipartFile image = mock(MultipartFile.class);
		given(image.getOriginalFilename()).willReturn("receipt. ");
		given(storeRepository.findById(any())).willReturn(Optional.of(Store.builder().build()));

		assertThatThrownBy(() -> ocrService.createValidation(1L, image, 1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("OCR 요청 이미지가 올바르지 않습니다.");
	}

	@Test
	void no_extension() throws IOException {
		MultipartFile image = mock(MultipartFile.class);
		given(image.getOriginalFilename()).willReturn("receipt.");
		given(storeRepository.findById(any())).willReturn(Optional.of(Store.builder().build()));

		assertThatThrownBy(() -> ocrService.createValidation(1L, image, 1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("OCR 요청 이미지가 올바르지 않습니다.");
	}

	@Test
	void no_dot_in_filename() throws IOException {
		MultipartFile image = mock(MultipartFile.class);
		given(image.getOriginalFilename()).willReturn("receipt");
		given(storeRepository.findById(any())).willReturn(Optional.of(Store.builder().build()));

		assertThatThrownBy(() -> ocrService.createValidation(1L, image, 1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("OCR 요청 이미지가 올바르지 않습니다.");
	}

	@Test
	void null_filename() throws IOException {
		// given
		MultipartFile image = mock(MultipartFile.class);
		given(image.getOriginalFilename()).willReturn(null);
		given(storeRepository.findById(any())).willReturn(Optional.of(Store.builder().build()));

		// when & then
		assertThatThrownBy(() -> ocrService.createValidation(1L, image, 1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("OCR 요청 이미지가 올바르지 않습니다.");
	}

	@Test
	void null_image() {
		// given
		Long storeId = 1L;
		Long userId = 1L;
		Store store = Store.builder().build();
		given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

		// when & then
		assertThatThrownBy(() -> ocrService.createValidation(storeId, null, userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("영수증 이미지가 등록되지 않았습니다."); // 메시지는 ErrorCode 따라 변경
	}

	// OCR 응답 생성 유틸
	private OcrResponseDto makeOcrResponse(String name, String subName) {
		OcrResponseDto.Name nameObj = new OcrResponseDto.Name();
		ReflectionTestUtils.setField(nameObj, "text", name);

		OcrResponseDto.SubName subNameObj = new OcrResponseDto.SubName();
		ReflectionTestUtils.setField(subNameObj, "text", subName);

		OcrResponseDto.StoreInfo storeInfo = new OcrResponseDto.StoreInfo();
		ReflectionTestUtils.setField(storeInfo, "name", nameObj);
		ReflectionTestUtils.setField(storeInfo, "subName", subNameObj);

		OcrResponseDto.Result result = new OcrResponseDto.Result();
		ReflectionTestUtils.setField(result, "storeInfo", storeInfo);

		OcrResponseDto.Receipt receipt = new OcrResponseDto.Receipt();
		ReflectionTestUtils.setField(receipt, "result", result);

		OcrResponseDto.Image image = new OcrResponseDto.Image();
		ReflectionTestUtils.setField(image, "receipt", receipt);

		OcrResponseDto response = new OcrResponseDto();
		ReflectionTestUtils.setField(response, "images", List.of(image));

		return response;
	}
}
