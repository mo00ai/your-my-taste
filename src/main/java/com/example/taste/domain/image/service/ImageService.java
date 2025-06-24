package com.example.taste.domain.image.service;

import static com.example.taste.common.exception.ErrorCode.*;
import static com.example.taste.domain.image.exception.ImageErrorCode.*;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.image.dto.ImageResponseDto;
import com.example.taste.domain.image.dto.S3ResponseDto;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.image.repository.ImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {
	private final S3Service s3Service;
	private final S3PresignedUrlService s3PresignedUrlService;
	private final ImageRepository imageRepository;

	@Transactional
	public Image saveImage(MultipartFile file, ImageType type) throws IOException {

		// 유효성 검사 등은 이쪽에서
		S3ResponseDto fileInfo = null;

		try {

			fileInfo = s3PresignedUrlService.upload(file);

			Image image = Image.builder()
				.type(type)
				.url(fileInfo.getStaticUrl())
				.uploadFileName(fileInfo.getUploadFileName())
				.originFileName(fileInfo.getOriginalFileName())
				.fileSize(file.getSize())
				.fileExtension(
					file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1).toLowerCase())
				.build();

			Image savedImage = imageRepository.save(image);

			return savedImage;

		} catch (Exception e) {

			//롤백
			if (fileInfo != null) {
				s3Service.delete(fileInfo.getUploadFileName());
			}
			throw new CustomException(FILE_UPLOAD_FAILED);
		}

	}

	@Transactional(readOnly = true)
	public ImageResponseDto findImage(Long imageId) {
		Image image = imageRepository.findById(imageId).orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));

		return new ImageResponseDto(image.getId(), image.getUrl(), image.getUploadFileName());
	}

	@Transactional
	public void update(Long imageId, ImageType type, MultipartFile file) throws IOException {

		Image image = imageRepository.findById(imageId).orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));

		// 유효성 검사 등은 이쪽에서
		S3ResponseDto fileInfo = null;

		try {

			fileInfo = s3PresignedUrlService.upload(file);

			image.update(
				fileInfo.getStaticUrl(),
				fileInfo.getUploadFileName(),
				fileInfo.getOriginalFileName(),
				file.getSize(),
				file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1).toLowerCase(),
				type
			);

		} catch (Exception e) {

			//롤백
			if (fileInfo != null) {
				s3Service.delete(fileInfo.getUploadFileName());
			}
			throw new CustomException(FILE_UPLOAD_FAILED);
		}
	}

	@Transactional
	public void deleteImage(Image image) {
		s3Service.delete(image.getUploadFileName());
		imageRepository.delete(image);

	}

}
