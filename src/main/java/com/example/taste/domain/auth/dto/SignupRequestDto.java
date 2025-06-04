package com.example.taste.domain.auth.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.Range;

import com.example.taste.common.annotation.ValidEnum;
import com.example.taste.domain.user.dto.request.UserFavorUpdateRequestDto;
import com.example.taste.domain.user.enums.Gender;
import com.example.taste.domain.user.enums.Role;

@Getter
public class SignupRequestDto {
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
	@Pattern(regexp = "^[a-zA-Z0-9ㄱ-ㅎ가-힣]+$", message = "닉네임은 영문자, 한글, 숫자만 입력 가능합니다.")
	private String nickname;

	@NotBlank(message = "주소는 필수값입니다.")
	private String address;

	@NotEmpty(message = "입맛 취향값은 필수값입니다.")
	@Size(min = 1, max = 5, message = "취향 입맛은 1개 이상 5개 이하로 입력해야 합니다.")
	private List<UserFavorUpdateRequestDto> favorList;

	@ValidEnum(target = Gender.class)
	private String gender;

	@Range(min = 1, max = 100)
	private Integer age;

	@ValidEnum(target = Role.class)
	@NotBlank(message = "권한은 필수값입니다.")
	private String role;
}
