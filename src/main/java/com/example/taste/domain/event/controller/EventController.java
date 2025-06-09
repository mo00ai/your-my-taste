package com.example.taste.domain.event.controller;

import static com.example.taste.domain.event.dto.response.EventSuccessCode.*;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.common.response.PageResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.event.dto.request.EventRequestDto;
import com.example.taste.domain.event.dto.request.EventUpdateRequestDto;
import com.example.taste.domain.event.dto.response.EventResponseDto;
import com.example.taste.domain.event.service.BoardEventService;
import com.example.taste.domain.event.service.EventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

	private final EventService eventService;
	private final BoardEventService boardEventService;

	// 이벤트 등록
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public CommonResponse<Void> createEvent(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody EventRequestDto requestDto) {
		eventService.createEvent(userDetails.getId(), requestDto);
		return CommonResponse.success(EVENT_CREATED);
	}

	// 이벤트 전체 조회
	@GetMapping
	public CommonResponse<PageResponse<EventResponseDto>> getEvent(
		@PageableDefault(size = 10, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return CommonResponse.ok(eventService.getEvents(pageable));
	}

	// 이벤트 수정
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{eventId}")
	public CommonResponse<?> updateEvent(
		@PathVariable Long eventId,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody EventUpdateRequestDto requestDto
	) {
		eventService.updateEvent(eventId, userDetails.getId(), requestDto);
		return CommonResponse.success(EVENT_UPDATED);
	}

	// 이벤트 삭제
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{eventId}")
	public CommonResponse<?> deleteEvent(
		@PathVariable Long eventId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		eventService.deleteEvent(eventId, userDetails.getId());
		return CommonResponse.success(EVENT_DELETED);
	}

	// 이벤트 신청
	@PostMapping("/{eventId}/boards/{boardId}")
	public CommonResponse<?> applyToEvent(
		@PathVariable Long eventId,
		@PathVariable Long boardId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		boardEventService.createBoardEvent(eventId, boardId, userDetails.getId());
		return CommonResponse.success(EVENT_APPLIED);
	}

	// 이벤트 신청 취소
	@DeleteMapping("/{eventId}/boards/{boardId}")
	public CommonResponse<?> cancelEventApplication(
		@PathVariable Long eventId,
		@PathVariable Long boardId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		boardEventService.cancelEventApplication(eventId, boardId, userDetails.getId());
		return CommonResponse.success(EVENT_APPLICATION_CANCELED);
	}
}
