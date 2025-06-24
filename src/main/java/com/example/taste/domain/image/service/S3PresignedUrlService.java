package com.example.taste.domain.image.service;

import static com.example.taste.domain.image.exception.ImageErrorCode.*;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.image.dto.S3ResponseDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3PresignedUrlService {

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@Value("${cloud.aws.region.static}")
	private String region;

	@Value("${cloud.aws.cloudfront.domain}")
	private String cloudFrontDomain;

	private final S3Presigner preSigner;

	private final WebClient webClient;

	private static final Tika tika = new Tika();

	public S3ResponseDto upload(MultipartFile file) {
		String originalFileName = Optional.ofNullable(file.getOriginalFilename())
			.map(StringUtils::cleanPath)
			.filter(name -> !name.isBlank())
			.orElse("file");
		String uploadFileName = UUID.randomUUID() + "_" + originalFileName;

		String mimeType;

		try {
			mimeType = tika.detect(file.getInputStream());
		} catch (IOException e) {
			throw new CustomException(FAILED_EXPORT_MIMETYPE);
		}

		PutObjectRequest objectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(uploadFileName)
			.contentType(mimeType)
			.build();

		PutObjectPresignRequest preSignedRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(15))
			.putObjectRequest(objectRequest)
			.build();

		PresignedPutObjectRequest presigned = preSigner.presignPutObject(preSignedRequest);
		String uploadUrl = presigned.url().toString();

		try {
			webClient.put()
				.uri(URI.create(uploadUrl))
				.contentType(MediaType.parseMediaType(mimeType))
				.bodyValue(file.getBytes())
				.exchangeToMono(response -> {
					if (response.statusCode().is2xxSuccessful()) {
						return response.toBodilessEntity();
					}
					return response.bodyToMono(String.class)
						.flatMap(body -> Mono.error(new CustomException(
							FAILED_PRE_SIGNED_URL_UPLOAD,
							"Presigned URL 업로드 실패: " + response.statusCode() + " - " + body
						)));
				})
				.block();
		} catch (IOException e) {
			throw new CustomException(FAILED_FILE_READ);
		}

		String staticUrl = UriComponentsBuilder.newInstance()
			.scheme("https")
			.host(cloudFrontDomain)
			.pathSegment(uploadFileName)
			.build()
			.toUriString();

		return new S3ResponseDto(
			staticUrl,
			uploadFileName,
			originalFileName
		);
	}
}


