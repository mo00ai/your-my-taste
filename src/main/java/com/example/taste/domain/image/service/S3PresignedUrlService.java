package com.example.taste.domain.image.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.taste.domain.image.dto.S3ResponseDto;

import lombok.RequiredArgsConstructor;
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

	private final S3Presigner preSigner;

	public S3ResponseDto upload(MultipartFile file) {

		String originalFileName = Optional.ofNullable(file.getOriginalFilename())
			.map(StringUtils::cleanPath)   // 경로·제어 문자 제거
			.filter(name -> !name.isBlank())
			.orElse("file");
		String uploadFileName = UUID.randomUUID() + "_" + originalFileName;

		PutObjectRequest objectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(uploadFileName)
			.contentType(file.getContentType())
			.build();

		PutObjectPresignRequest preSignedRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(15))
			.putObjectRequest(objectRequest)
			.build();

		PresignedPutObjectRequest presigned = preSigner.presignPutObject(preSignedRequest);

		String staticUrl = UriComponentsBuilder.newInstance()
			.scheme("https")
			.host(bucket + ".s3." + region + ".amazonaws.com")
			.path("/" + uploadFileName)
			.build()
			.encode(StandardCharsets.UTF_8)
			.toUriString();

		String uploadUrl = presigned.url().toString();

		return new S3ResponseDto(
			uploadUrl,         // preSigned put url
			uploadFileName,    // 실제 S3 key
			originalFileName,  // 원본 파일명
			staticUrl          // 최종 접근용 url query 없는 정적 주소
		);
	}
}


