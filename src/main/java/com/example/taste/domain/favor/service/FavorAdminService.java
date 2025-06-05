package com.example.taste.domain.favor.service;

import static com.example.taste.domain.favor.exception.FavorErrorCode.NOT_FOUND_FAVOR;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.favor.dto.request.FavorAdminRequestDto;
import com.example.taste.domain.favor.dto.response.FavorAdminResponseDto;
import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.favor.repository.FavorRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavorAdminService {
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
		Favor favor = favorRepository.findById(favorId).orElseThrow(
			() -> new CustomException(NOT_FOUND_FAVOR)
		);
		favor.update(requestDto.getFavorName());
	}

	@Transactional
	public void deleteFavor(Long favorId) {
		Favor favor = favorRepository.findById(favorId).orElseThrow(
			() -> new CustomException(NOT_FOUND_FAVOR)
		);
		favorRepository.delete(favor);
	}
}
