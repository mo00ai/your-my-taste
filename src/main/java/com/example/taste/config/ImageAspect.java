package com.example.taste.config;

import static com.example.taste.common.constant.ImageConst.*;
import static com.example.taste.common.exception.ErrorCode.*;
import static com.example.taste.domain.image.exception.ImageErrorCode.*;

import java.io.IOException;
import java.util.List;

import org.apache.tika.Tika;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.image.enums.ImageExtension;
import com.example.taste.domain.image.enums.ImageType;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
@Component
public class ImageAspect {

	private static final Tika tika = new Tika();

	//이미지 크기, 개수 ,타입 체크 AOP @ImageValid 붙은 컨트롤러에만 적용
	//맞는 타입체크하고 검사함
	@Around("@annotation(com.example.taste.common.annotation.ImageValid)")
	public Object validParam(ProceedingJoinPoint point) throws Throwable {

		Class<?> targetClass = point.getTarget().getClass();
		ImageType type = ImageType.fromControllerClass(targetClass);

		System.out.println("이미지 타입: " + type.toString());

		for (Object arg : point.getArgs()) {
			if (arg instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof MultipartFile) {
				List<MultipartFile> fileList = (List<MultipartFile>)list;
				validateFileType(fileList, type);
			} else if (arg instanceof MultipartFile file && !file.isEmpty()) {
				validateFileType(List.of(file), type);  // List로 감싸서 재사용
			}

		}
		return point.proceed();
	}

	//이미지가 사용되는 유형 검사
	//게시글,리뷰,사용자 etc
	private void validateFileType(List<MultipartFile> files, ImageType type) {
		if (files.size() > type.getMaxCount()) {

			if (type != ImageType.BOARD) {
				throw new CustomException(ONLY_ONE_IMAGE_ALLOWED);
			}

			throw new CustomException(FIVE_IMAGES_ALLOWED);

		}
		validateFiles(files);
	}

	//이미지 검사
	private void validateFiles(List<MultipartFile> files) {
		for (MultipartFile file : files) {
			if (file.getSize() > MAX_IMAGE_SIZE) {
				throw new CustomException(INVALID_IMAGE_SIZE);
			}

			String filename = file.getOriginalFilename();
			if (filename == null || !filename.contains(".")) {
				throw new CustomException(INVALID_FILE_EXTENSION);
			}

			String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
			if (!ImageExtension.isValidExtension(extension)) {
				throw new CustomException(INVALID_FILE_EXTENSION);
			}

			try {
				String mimeType = tika.detect(file.getInputStream());
				if (!ImageExtension.isValidMimeType(mimeType)) {
					throw new CustomException(INVALID_MIME_TYPE);
				}
			} catch (IOException e) {
				throw new CustomException(FILE_READ_ERROR);
			}
		}
	}

}
