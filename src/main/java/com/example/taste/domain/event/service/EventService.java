package com.example.taste.domain.event.service;

import static com.example.taste.domain.event.exception.EventErrorCode.*;
import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.common.response.PageResponse;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.event.dto.request.EventRequestDto;
import com.example.taste.domain.event.dto.request.EventUpdateRequestDto;
import com.example.taste.domain.event.dto.response.EventResponseDto;
import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.event.repository.EventRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {
	private final EventRepository eventRepository;
	private final UserRepository userRepository;

	@Transactional
	public void createEvent(Long userId, EventRequestDto requestDto) {
		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		Event entity = EventRequestDto.toEntity(user, requestDto);
		eventRepository.save(entity);
	}

	@Transactional(readOnly = true)
	public PageResponse<EventResponseDto> getEvents(Pageable pageable) {
		Page<EventResponseDto> dtoPage = eventRepository.findAll(pageable)
			.map(EventResponseDto::new);
		return PageResponse.from(dtoPage);
	}

	@Transactional
	public void updateEvent(Long eventId, Long userId, EventUpdateRequestDto requestDto) {
		Event event = findById(eventId);
		checkUser(userId, event);
		event.update(requestDto);

	}

	@Transactional
	public void deleteEvent(Long eventId, Long userId) {
		Event event = findById(eventId);
		checkUser(userId, event);
		eventRepository.delete(event);
	}

	protected void checkUser(Long userId, Event event) {
		if (!event.getUser().getId().equals(userId)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}

	}

	@Transactional(readOnly = true)
	public Event findById(Long eventId) {
		return eventRepository.findById(eventId).orElseThrow(() -> new CustomException(NOT_FOUND_EVENT));
	}

	@Transactional(readOnly = true)
	public List<Event> findEndedEventList(LocalDate endDate) {
		return eventRepository.findEndedEventList(endDate);
	}

	@Transactional(readOnly = true)
	public Optional<Board> findWinningBoard(Long eventId) {
		return eventRepository.findWinningBoard(eventId);
	}

}
