package com.example.taste.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.Range;

import com.example.taste.domain.user.dto.request.UserFavorUpdateListRequestDto;

@Getter
public class SignUpRequestDto {
	@NotBlank(message = "이메일은 필수값입니다.")
	@Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "유효한 이메일 형식이 아닙니다.")
	private String email;

	@Setter
	@NotBlank(message = "비밀번호는 필수값입니다.")
	@Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자리 이하입니다.")
	@Pattern(regexp = ".*\\d.*", message = "비밀번호는 숫자를 포함해야 합니다.")
	@Pattern(regexp = ".*[a-zA-Z].*", message = "비밀번호는 영문자를 포함해야 합니다.")
	@Pattern(regexp = ".*[@_\\-#$].*", message = "비밀번호는 특수문자를 포함해야 합니다.")
	private String password;

	@NotBlank(message = "닉네임은 필수값입니다.")
	@Size(min = 2, max = 20, message = "닉네임은 2글자 이상 20글자 이하입니다.")
	@Pattern(regexp = "^[a-z|A-Z|0-9|ㄱ-ㅎ|가-힣]+$", message = "닉네임은 영문자, 한글, 숫자만 입력 가능합니다.")
	private String nickname;
	private String address;
	private UserFavorUpdateListRequestDto favorList;

	private String gender;        // TODO: enum valid 적용 필요

	@Range(min = 1, max = 100)
	private Integer age;

	@NotBlank(message = "권한은 필수값입니다.")
	private String role;            // TODO: enum valid 적용 필요
}
