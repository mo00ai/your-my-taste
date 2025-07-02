package com.example.taste.domain.image.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.taste.domain.image.dto.S3ResponseDto;
import com.example.taste.property.AbstractIntegrationTest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

public class S3PresignedUrlServiceTest extends AbstractIntegrationTest {

	private MockWebServer mockWebServer;
	private S3PresignedUrlService service;

	private final S3Presigner presigner = mock(S3Presigner.class);

	@BeforeEach
	void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();

		WebClient webClient = WebClient.builder()
			.baseUrl(mockWebServer.url("/").toString())
			.build();

		service = new S3PresignedUrlService(presigner, webClient);

		// 값 주입
		ReflectionTestUtils.setField(service, "bucket", "test-bucket");
		ReflectionTestUtils.setField(service, "region", "ap-northeast-2");
		ReflectionTestUtils.setField(service, "cloudFrontDomain", "cdn.example.com");
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	void upload_success_returnS3ResponseDto() throws IOException {
		// given
		String fileName = "test.jpg";
		MockMultipartFile file = new MockMultipartFile("file", fileName, "image/jpeg", "mock image".getBytes());

		String presignedPath = "upload/" + fileName;
		URL presignedUrl = mockWebServer.url("/" + presignedPath).url();

		mockWebServer.enqueue(
			new MockResponse()
				.setResponseCode(200)
		);

		PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
		when(presignedRequest.url()).thenReturn(presignedUrl);

		when(presigner.presignPutObject(any(PutObjectPresignRequest.class)))
			.thenReturn(presignedRequest);

		// when
		S3ResponseDto response = service.upload(file);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getOriginalFileName()).isEqualTo(fileName);
		assertThat(response.getStaticUrl()).contains("cdn.example.com");
	}
}
