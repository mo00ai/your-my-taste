package com.example.taste.domain.board.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.board.entity.FcfsInformation;
import com.example.taste.domain.board.repository.FcfsInformationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FcfsInformationService {

	private final FcfsInformationRepository fcfsInformationRepository;
	private final RedisService redisService;

	@Transactional
	public void saveFcfsInfoToDB(String key, Long boardId) {
		if (!fcfsInformationRepository.existsByBoardId(boardId)) {
			List<FcfsInformation> infos = redisService.getZSetRange(key).stream()
				.map(object -> ((Number)object).longValue())
				.map(id -> new FcfsInformation(boardId, id))
				.toList();
			fcfsInformationRepository.saveAll(infos);
		}
	}
}
