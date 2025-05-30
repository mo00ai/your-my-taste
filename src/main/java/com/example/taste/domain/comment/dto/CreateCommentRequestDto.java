package com.example.taste.domain.comment.dto;

import com.example.taste.domain.comment.entity.Comment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentRequestDto {

	@NotNull
	//@Pattern(regexp = "^(?!.*(<script|</script|<img|<a\\s|select\\s|union\\s|insert\\s|update\\s|delete\\s|drop\\s|--|\\bor\\b|\\band\\b)).*$", message = "허용되지 않는 문자가 포함되어있습니다.")
	//굳이?
	private String content;
	private Comment pareantComment;
}
