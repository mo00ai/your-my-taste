package com.example.taste.domain.event.service;

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
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.event.dto.request.EventRequestDto;
import com.example.taste.domain.event.dto.request.EventUpdateRequestDto;
import com.example.taste.domain.event.dto.response.EventResponseDto;
import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.event.repository.EventRepository;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {
	private final EntityFetcher entityFetcher;
	private final EventRepository eventRepository;

	@Transactional
	public void createEvent(Long userId, EventRequestDto requestDto) {
		User user = entityFetcher.getUserOrThrow(userId);
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
		return entityFetcher.getEventOrThrow(eventId);
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
