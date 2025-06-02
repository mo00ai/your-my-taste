package com.example.taste.domain.image.service;

import static com.example.taste.domain.image.exception.ImageErrorCode.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.domain.image.dto.ImageResponseDto;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.image.repository.BoardImageRepository;
import com.example.taste.domain.image.repository.ImageRepository;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class ImageService {

	private final ImageRepository imageRepository;
	private final BoardImageRepository boardImageRepository;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@Value("${cloud.aws.region.static}")
	private String region;

	private final S3Client s3Client;

	//aws에 업로드
	public List<Map<String, Object>> uploadFile(List<MultipartFile> fileList) throws IOException {
		List<Map<String, Object>> returnList = new ArrayList<>(); // 업로드한 사진 정보 담아서 리턴

		for (MultipartFile file : fileList) {
			Map<String, Object> fileInfo = new HashMap<>();

			String originalFilename = file.getOriginalFilename();
			String uploadFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
			Long fileSize = file.getSize();
			String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();

			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)  // 업로드할 대상 버킷 이름
				.key(uploadFilename)  // 버킷 내 저장할 경로 (위에서 만든 고유 파일명)
				.contentType(file.getContentType()) // 타입 image/png
				.build();
			s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

			fileInfo.put("url", "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + uploadFilename);
			fileInfo.put("originalFilename", originalFilename);
			fileInfo.put("uploadFilename", uploadFilename);
			fileInfo.put("fileSize", fileSize);
			fileInfo.put("fileExtension", fileExtension);
			fileInfo.put("contentType", file.getContentType());

			returnList.add(fileInfo);
		}
		return returnList;
	}

	//aws에 사진 업로드 후 db에 저장
	public void saveImage(List<MultipartFile> files, ImageType type) {

		List<String> uploadedKey = new ArrayList<>(); // 업로드 성공한 키 저장
		try {// db에 저장 실패 or aws 에 업로드 실패 시 에러
			uploadFile(files).forEach(data -> {
				String url = (String)data.get("url");
				String originalName = (String)data.get("originalFilename");
				String uploadName = (String)data.get("uploadFilename");
				String extension = (String)data.get("fileExtension");
				Long size = (Long)data.get("fileSize");

				Image image = Image.builder()
					.type(type)
					.url(url)
					.originFileName(originalName)
					.uploadFileName(uploadName)
					.fileExtension(extension)
					.build();

				imageRepository.save(image);

				uploadedKey.add(uploadName);
			});
		} catch (Exception e) {
			for (String key : uploadedKey) {
				DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder() // aws 에 이미 올라간 이미지 삭제
					.bucket(bucketName)  // 연결 된 대상 버킷 이름
					.key(key)  // 버킷 내 삭제할 객체 키
					.build();
				s3Client.deleteObject(deleteObjectRequest);
			}
			throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED); // 리뷰 저장 롤백위해 오류 날림
		}
	}

	public ImageResponseDto findImage(Long imageId) {
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));

		return new ImageResponseDto(image);
	}

	public void update(Long imageId, List<MultipartFile> fileList) {

		delete(findImage(imageId).getImgKey());// 기존 사진 삭제
		fileSave(fileList, targetId, imageType);
	}

	public void delete(String keys) {
		for (String key : keys) {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
				.bucket(bucketName)  // 연결 된 대상 버킷 이름
				.key(key)  // 버킷 내 삭제할 객체 키
				.build();
			s3Client.deleteObject(deleteObjectRequest);
		}
		imageRepository.deleteAll(
			imageRepository.findByTargetIdAndTypeElseThrow(targetId, imageType));
	}

}
