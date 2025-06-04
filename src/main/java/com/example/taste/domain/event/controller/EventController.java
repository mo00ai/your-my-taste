package com.example.taste.domain.event.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.event.service.EventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

	private final EventService eventService;

	// 이벤트 등록
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public CommonResponse<?> createEvent() {
		return CommonResponse.ok();
	}

	// 이벤트 전체 조회
	@GetMapping
	public CommonResponse<?> getEvent() {
		return CommonResponse.ok();
	}

	// 이벤트 수정
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{eventId}")
	public CommonResponse<?> updateEvent() {
		return CommonResponse.ok();
	}

	// 이벤트 삭제
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{eventId}")
	public CommonResponse<?> deleteEvent() {
		return CommonResponse.ok();
	}

	// 이벤트 신청
	@PostMapping("/{eventId}/boards/{boardId}")
	public CommonResponse<?> applyToEvent() {
		return CommonResponse.ok();
	}

	// 이벤트 신청 취소
	@DeleteMapping("/{eventId}/boards/{boardId}")
	public CommonResponse<?> cancelEventApplication() {
		return CommonResponse.ok();
	}
}
