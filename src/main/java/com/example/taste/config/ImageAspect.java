package com.example.taste.config;

import static com.example.taste.common.constant.ImageConst.*;

import java.io.IOException;
import java.util.List;

import org.apache.tika.Tika;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
@Component
public class ImageAspect {

	private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png");
	private static final List<String> ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png");
	private static final Tika tika = new Tika();

	//이미지 크기, 개수 ,타입 체크 AOP @ImageValid 붙은 컨트롤러에만 적용
	//맞는 타입체크하고 검사함
	@Around("@annotation(com.example.taste.common.annotation.ImageValid)")
	public Object validParam(ProceedingJoinPoint point) throws Throwable {

		Class<?> targetClass = point.getTarget().getClass(); // 실제 컨트롤러 클래스
		boolean isBoard = targetClass.getSimpleName().contains("Board"); // 또는 .contains("Board")

		for (Object arg : point.getArgs()) {
			// 리스트 x , 리스트 비어있음, 타입이 다르면 for문 탈출
			if (!(arg instanceof List<?> list) || list.isEmpty() || !(list.get(0) instanceof MultipartFile)) {
				continue;
			}
			validateFile((List<MultipartFile>)list, isBoard); // 이미지 검사
		}
		return point.proceed();
	}

	//실제 이미지 valid 메소드

	private void validateFile(List<MultipartFile> fileList, boolean isBoard) {

		// 이미지 개수 검사
		if (isBoard) {
			if (fileList.size() > MAX_IMAGE_CNT) {
				throw new CustomException(ErrorCode.INVALID_IMAGE_COUNT);
			}
		} else {
			if (fileList.size() > 1) {
				throw new CustomException(ErrorCode.ONLY_ONE_IMAGE_ALLOWED);
			}
		}

		for (MultipartFile file : fileList) {
			// 크기 제한
			if (file.getSize() > MAX_IMAGE_SIZE) {
				throw new CustomException(ErrorCode.INVALID_IMAGE_SIZE);
			}

			// 확장자 검사
			String originalFilename = file.getOriginalFilename();
			if (originalFilename == null || !originalFilename.contains(".")) {
				throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
			}

			String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
			if (!ALLOWED_EXTENSIONS.contains(extension)) {
				throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
			}

			try {
				String mimeType = tika.detect(file.getInputStream());
				if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
					throw new CustomException(ErrorCode.INVALID_MIME_TYPE);
				}

			} catch (IOException ex) {
				throw new CustomException(ErrorCode.FILE_READ_ERROR);
			}
		}

	}
}
