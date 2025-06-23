package com.example.taste.domain.embedding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmbeddingRequest {

	@NotBlank(message = "텍스트는 필수입니다")
	@Size(max = 500, message = "텍스트는 500자 이하여야 합니다")
	private String text;
}
