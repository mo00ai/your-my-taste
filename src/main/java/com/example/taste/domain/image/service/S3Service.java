package com.example.taste.domain.image.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@Value("${cloud.aws.region.static}")
	private String region;

	private final S3Client s3Client;

	// public S3ResponseDto upload(MultipartFile file) throws IOException {
	//
	// 	// String originalFileName = file.getOriginalFilename();
	// 	// String uploadFileName = UUID.randomUUID() + "_" + originalFileName;
	//
	// 	String originalFileName = Optional.ofNullable(file.getOriginalFilename())
	// 		.map(StringUtils::cleanPath)   // 경로·제어 문자 제거
	// 		.filter(name -> !name.isBlank())
	// 		.orElse("file");
	// 	String uploadFileName = UUID.randomUUID() + "_" + originalFileName;
	//
	// 	PutObjectRequest request = PutObjectRequest.builder()
	// 		.bucket(bucketName)
	// 		.key(uploadFileName)
	// 		.contentType(file.getContentType())
	// 		.build();
	//
	// 	s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
	//
	// 	UriComponents uri = UriComponentsBuilder.newInstance()
	// 		.scheme("https")
	// 		.host(bucketName + ".s3." + region + ".amazonaws.com")
	// 		.path("/" + uploadFileName)
	// 		.build()
	// 		.encode(StandardCharsets.UTF_8);
	// 	;
	//
	// 	String uploadUrl = uri.toUriString();
	//
	// 	return new S3ResponseDto(
	// 		uploadUrl,
	// 		uploadFileName,
	// 		originalFileName
	// 	);
	// }

	public void delete(String uploadFileName) {
		DeleteObjectRequest request = DeleteObjectRequest.builder()
			.bucket(bucketName)
			.key(uploadFileName)
			.build();
		s3Client.deleteObject(request);
	}
}
