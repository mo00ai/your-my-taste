package com.example.taste.domain.image.service;

import static com.example.taste.common.exception.ErrorCode.FILE_UPLOAD_FAILED;

import java.io.IOException;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.image.dto.ImageResponseDto;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.image.repository.ImageRepository;

@Service
@RequiredArgsConstructor
public class ImageService {
	private final EntityFetcher entityFetcher;
	private final S3Service s3Service;
	private final ImageRepository imageRepository;

	@Transactional
	public Image saveImage(MultipartFile file, ImageType type) throws IOException {

		// 유효성 검사 등은 이쪽에서
		Map<String, String> fileInfo = null;

		try {

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

			return savedImage;

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
		Image image = entityFetcher.getImageOrThrow(imageId);

		return new ImageResponseDto(image.getId(), image.getUrl(), image.getUploadFileName());
	}

	@Transactional
	public void update(Long imageId, ImageType type, MultipartFile file) throws IOException {

		Image image = entityFetcher.getImageOrThrow(imageId);
		// 유효성 검사 등은 이쪽에서
		Map<String, String> fileInfo = null;

		try {

			fileInfo = s3Service.upload(file);

			image.update(
				fileInfo.get("url"),
				fileInfo.get("uploadFileName"),
				fileInfo.get("originalFileName"),
				file.getSize(),
				file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1).toLowerCase(),
				type
			);

		} catch (Exception e) {

			//롤백
			if (fileInfo != null) {
				s3Service.delete(fileInfo.get("uploadFileName"));
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
