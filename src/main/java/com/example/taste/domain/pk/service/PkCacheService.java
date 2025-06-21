package com.example.taste.domain.pk.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.pk.dto.response.PkCriteriaResponseDto;
import com.example.taste.domain.pk.entity.PkCriteria;
import com.example.taste.domain.pk.repository.PkCriteriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PkCacheService {
	private final PkCriteriaRepository pkCriteriaRepository;

	@Cacheable(value = "pkCriteriaCache", key = "'all'", cacheManager = "redisCacheManager")
	@Transactional(readOnly = true)
	public List<PkCriteriaResponseDto> findAllPkCriteria() {
		List<PkCriteria> all = pkCriteriaRepository.findAll();
		return all.stream().map(pk -> PkCriteriaResponseDto.builder()
				.id(pk.getId())
				.type(pk.getType().name())
				.point(pk.getPoint())
				.active(pk.isActive())
				.build())
			.collect(Collectors.toList());
	}
}
