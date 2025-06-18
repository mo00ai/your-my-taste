package com.example.taste.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
public class UserUpdateRequestDto {
	private String nickname;

	private String address;

	@NotBlank(message = "기존 비밀번호는 필수값입니다.")
	private String oldPassword;

	@Setter
	@Size(min = 8, max = 20, message = "새 비밀번호는 8자 이상 20자리 이하입니다.")
	@Pattern(regexp = ".*\\d.*", message = "새 비밀번호는 숫자를 포함해야 합니다.")
	@Pattern(regexp = ".*[a-zA-Z].*", message = "새 비밀번호는 영문자를 포함해야 합니다.")
	@Pattern(regexp = ".*[@_\\-#$].*", message = "새 비밀번호는 특수문자를 포함해야 합니다.")
	private String newPassword;
}
