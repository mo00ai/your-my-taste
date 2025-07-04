package com.example.taste.domain.image.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.image.dto.S3ResponseDto;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.image.repository.ImageRepository;
import com.example.taste.fixtures.ImageFixture;

@ExtendWith(MockitoExtension.class)
class ImageServiceUnitTest {

	@InjectMocks
	private ImageService imageService;

	@Mock
	private S3PresignedUrlService s3PresignedUrlService;

	@Mock
	private S3Service s3Service;

	@Mock
	private ImageRepository imageRepository;

	@Test
	void saveImage_success() throws IOException {
		// given
		MultipartFile file = mock(MultipartFile.class);
		given(file.getOriginalFilename()).willReturn("image.jpg");
		given(file.getSize()).willReturn(1024L);

		S3ResponseDto s3Dto = new S3ResponseDto("origin.jpg", "upload.jpg", "https://cdn.com/upload.jpg");
		given(s3PresignedUrlService.upload(file)).willReturn(s3Dto);

		Image saved = ImageFixture.create();

		given(imageRepository.save(any())).willReturn(saved);

		// when
		Image result = imageService.saveImage(file, ImageType.BOARD);

		// then
		assertThat(result.getUploadFileName()).isEqualTo("uploadfile");
		verify(imageRepository).save(any());
	}

	@Test
	void saveImage_fail_thenRollbackS3() throws IOException {
		// given
		MultipartFile file = mock(MultipartFile.class);
		given(file.getOriginalFilename()).willReturn("test.jpg");
		given(file.getSize()).willReturn(100L);

		S3ResponseDto s3Dto = new S3ResponseDto("origin", "upload", "https://cdn/upload");
		given(s3PresignedUrlService.upload(file)).willReturn(s3Dto);

		given(imageRepository.save(any())).willThrow(RuntimeException.class);

		// when & then
		assertThatThrownBy(() -> imageService.saveImage(file, ImageType.BOARD))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("파일 업로드 실패했습니다.");

		verify(s3Service).delete("upload");
	}

	@Test
	void findImage_fail_thenThrowException() {
		// given
		given(imageRepository.findById(any())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> imageService.findImage(1L))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("이미지를 찾을 수 없습니다");
	}
}
