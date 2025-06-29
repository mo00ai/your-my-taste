package com.example.taste.domain.board.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.FcfsInformation;
import com.example.taste.domain.board.repository.FcfsInformationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FcfsPersistService {

	private final FcfsInformationRepository fcfsInformationRepository;
	private final RedisService redisService;

	@Transactional
	public void saveFcfsInfoToDB(String key, Board board) {
		if (!fcfsInformationRepository.existsByBoardId(board.getId())) {
			List<FcfsInformation> infos = redisService.getZSetRange(key).stream()
				.map(object -> ((Number)object).longValue())
				.map(id -> new FcfsInformation(board.getId(), id))
				.toList();
			fcfsInformationRepository.saveAll(infos);
		}
	}
}
