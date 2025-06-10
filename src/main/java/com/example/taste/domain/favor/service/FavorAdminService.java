package com.example.taste.domain.favor.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.favor.dto.request.FavorAdminRequestDto;
import com.example.taste.domain.favor.dto.response.FavorAdminResponseDto;
import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.favor.repository.FavorRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavorAdminService {
	private final EntityFetcher entityFetcher;
	private final FavorRepository favorRepository;

	@Transactional
	public void createFavor(List<FavorAdminRequestDto> requestDtoList) {
		requestDtoList.forEach(favor -> {
			if (!favorRepository.existsByName(favor.getFavorName())) {
				favorRepository.save(new Favor(favor.getFavorName()));
			}
		});
	}

	public List<FavorAdminResponseDto> getAllFavor() {
		return favorRepository.findAll()
			.stream()
			.map(FavorAdminResponseDto::new)
			.toList();
	}

	@Transactional
	public void updateFavor(Long favorId, FavorAdminRequestDto requestDto) {
		Favor favor = entityFetcher.getFavorOrThrow(favorId);
		favor.update(requestDto.getFavorName());
	}

	@Transactional
	public void deleteFavor(Long favorId) {
		Favor favor = entityFetcher.getFavorOrThrow(favorId);
		favorRepository.delete(favor);
	}
}
