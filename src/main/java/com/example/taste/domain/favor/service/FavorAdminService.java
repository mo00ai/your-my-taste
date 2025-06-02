package com.example.taste.domain.favor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.favor.dto.request.FavorListCreateRequestDto;
import com.example.taste.domain.favor.dto.request.FavorRequestDto;
import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.favor.repository.FavorRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavorAdminService {
	private final FavorRepository favorRepository;

	@Transactional
	public void createFavor(FavorListCreateRequestDto requestDto) {
		requestDto.getFavorList().forEach(favor -> {
			if (favorRepository.findByName(favor.getFavorName()) == null) {
				favorRepository.save(new Favor(favor.getFavorName()));
			}
		});
	}

	@Transactional
	public void updateFavor(FavorRequestDto requestDto) {
		Favor favor = favorRepository.findByName(requestDto.getFavorName());
		favor.update(requestDto.getFavorName());
	}
}
