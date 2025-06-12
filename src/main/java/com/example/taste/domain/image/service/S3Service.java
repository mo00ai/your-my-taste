package com.example.taste.domain.image.service;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@Value("${cloud.aws.region.static}")
	private String region;

	private final S3Client s3Client;

	public Map<String, String> upload(MultipartFile file) throws IOException {

		String originalFilename = file.getOriginalFilename();
		String uploadFilename = UUID.randomUUID() + "_" + originalFilename;

		PutObjectRequest request = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(uploadFilename)
			.contentType(file.getContentType())
			.build();

		s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

		URL s3Url = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(uploadFilename));

		return Map.of(
			"url", s3Url.toString(),
			"uploadFileName", uploadFilename,
			"originalFileName", originalFilename
		);
	}

	public void delete(String uploadFileName) {
		DeleteObjectRequest request = DeleteObjectRequest.builder()
			.bucket(bucketName)
			.key(uploadFileName)
			.build();
		s3Client.deleteObject(request);
	}
}
