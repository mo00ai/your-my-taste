package com.example.taste.domain.review.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateValidationRequest {

	private MultipartFile image;  // 파일 필드

	@NotBlank
	private String description;
}
