package com.example.taste.domain.event.service;

import org.springframework.stereotype.Service;

import com.example.taste.domain.event.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {
	private final EventRepository eventRepository;
	
}
