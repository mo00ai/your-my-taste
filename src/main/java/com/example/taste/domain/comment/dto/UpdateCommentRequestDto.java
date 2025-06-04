package com.example.taste.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentRequestDto {
	@NotBlank
	@Pattern(regexp = "^(?!.*(<script|</script|<img|<a\\s|select\\s|union\\s|insert\\s|update\\s|delete\\s|drop\\s|--|\\bor\\b|\\band\\b)).*$", message = "허용되지 않는 문자가 포함되어있습니다.")
	private String contents;
}
