package com.example.taste.domain.image.service;

import static com.example.taste.common.exception.ErrorCode.*;
import static com.example.taste.domain.image.exception.ImageErrorCode.*;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.image.dto.ImageResponseDto;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.image.repository.ImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {

	private final S3Service s3Service;
	private final ImageRepository imageRepository;

	@Transactional
	public ImageResponseDto saveImage(MultipartFile file, ImageType type) throws IOException {

		// 유효성 검사 등은 이쪽에서
		Map<String, String> fileInfo = null;

		try {

			System.out.println("트라이 캐치 들어옴");

			fileInfo = s3Service.upload(file);

			Image image = Image.builder()
				.type(type)
				.url(fileInfo.get("url"))
				.uploadFileName(fileInfo.get("uploadFileName"))
				.originFileName(fileInfo.get("originalFileName"))
				.fileSize(file.getSize())
				.fileExtension(
					file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1).toLowerCase())
				.build();

			Image savedImage = imageRepository.save(image);

			return ImageResponseDto.builder()
				.image(savedImage)
				.build();

		} catch (Exception e) {

			//롤백
			if (fileInfo != null) {
				s3Service.delete(fileInfo.get("uploadFileName"));
			}
			throw new CustomException(FILE_UPLOAD_FAILED);
		}

	}

	@Transactional(readOnly = true)
	public ImageResponseDto findImage(Long imageId) {
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));

		return ImageResponseDto.builder()
			.image(image)
			.build();
	}

	@Transactional
	public void update(Long imageId, ImageType type, MultipartFile file) throws IOException {
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));

		deleteImage(image);// 기존 사진 삭제
		saveImage(file, type);
	}

	@Transactional
	public void deleteImage(Image image) {
		s3Service.delete(image.getUploadFileName());
		imageRepository.delete(image);
	}

}
