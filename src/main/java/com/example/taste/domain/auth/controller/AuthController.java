package com.example.taste.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.auth.dto.DeleteWebPushInfoDto;
import com.example.taste.domain.auth.dto.SigninRequestDto;
import com.example.taste.domain.auth.dto.SignupRequestDto;
import com.example.taste.domain.auth.dto.UserResponseDto;
import com.example.taste.domain.auth.service.AuthService;
import com.example.taste.domain.notification.service.WebPushService;
import com.example.taste.domain.user.entity.CustomUserDetails;
import com.example.taste.domain.user.facade.UserFacade;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final UserFacade userFacade;
	private final AuthService authService;
	private final WebPushService webPushService;

	// web push를 위해 유저에세 vapid 공개키를 전송할 필요가 있음.
	@Value("${vapid.public}")
	private String vapidPublicKey;

	@PostMapping("/signup")
	public CommonResponse<Void> signup(
		@RequestPart(name = "data") @Valid SignupRequestDto requestDto,
		@RequestPart(name = "file", required = false) MultipartFile file) {
		userFacade.signup(requestDto, file);

		return CommonResponse.ok();
	}

	// vapid 공개키를 전송하기 위해 반환 객체를 설정하였음.
	@PostMapping("/signin")
	public CommonResponse<UserResponseDto> signin(
		HttpServletRequest httpRequest, @RequestBody SigninRequestDto requestDto) {
		authService.signin(httpRequest, requestDto);

		return CommonResponse.ok(new UserResponseDto(vapidPublicKey));
	}

	@PostMapping("/signout")
	public CommonResponse<Void> signout(
		HttpServletRequest httpRequest, HttpServletResponse httpResponse,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		// 로그아웃 시 등록된 web push 정보를 삭제
		@RequestBody DeleteWebPushInfoDto dto) {
		webPushService.deleteSubscription(userDetails.getId(), dto.getEndpoint());
		authService.signout(httpRequest, httpResponse);

		return CommonResponse.ok();
	}
}
