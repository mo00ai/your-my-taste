package com.example.taste.domain.pk.service;

import static com.example.taste.domain.pk.exception.PkErrorCode.*;
import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.pk.dto.request.PkUpdateRequestDto;
import com.example.taste.domain.pk.dto.response.PkCriteriaResponseDto;
import com.example.taste.domain.pk.entity.PkCriteria;
import com.example.taste.domain.pk.entity.PkLog;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.repository.PkCriteriaRepository;
import com.example.taste.domain.pk.repository.PkLogRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PkService {

	private final PkCriteriaRepository pkCriteriaRepository;
	private final PkLogRepository pkLogRepository;
	private final UserRepository userRepository;
	private final UserService userService;

	@Transactional
	public PkCriteriaResponseDto savePkCriteria(String type, Integer point) {

		PkType pkType = PkType.valueOf(type.toUpperCase());

		if (pkCriteriaRepository.existsByType(pkType)) {
			throw new CustomException(DUPLICATE_PK_TYPE);
		}

		PkCriteria pkCriteria = PkCriteria.builder()
			.type(pkType)
			.point(point)
			.active(true)
			.build();

		PkCriteria saved = pkCriteriaRepository.save(pkCriteria);

		return PkCriteriaResponseDto.builder()
			.id(saved.getId())
			.type(saved.getType().toString())
			.point(saved.getPoint())
			.isActive(saved.isActive())
			.build();
	}

	@Transactional(readOnly = true)
	public List<PkCriteriaResponseDto> findAllPkCriteria() {

		List<PkCriteria> all = pkCriteriaRepository.findAll();

		return all.stream().map(pk -> PkCriteriaResponseDto.builder()
				.id(pk.getId())
				.type(pk.getType().name())
				.point(pk.getPoint())
				.isActive(pk.isActive())
				.build()
			)
			.collect(Collectors.toList());

	}

	@Transactional
	public void updatePkCriteria(Long id, PkUpdateRequestDto dto) {

		PkCriteria pkCriteria = pkCriteriaRepository.findById(id)
			.orElseThrow(() -> new CustomException(PK_CRITERIA_NOT_FOUND));

		pkCriteria.update(dto);

	}

	@Transactional
	public void deletePkCriteria(Long id) {

		PkCriteria pkCriteria = pkCriteriaRepository.findById(id)
			.orElseThrow(() -> new CustomException(PK_CRITERIA_NOT_FOUND));

		pkCriteria.delete();

	}

	@Transactional
	public void savePkLog(Long userId, PkType type) {

		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

		PkCriteria pkCriteria = pkCriteriaRepository.findByType(type)
			.orElseThrow(() -> new CustomException(PK_CRITERIA_NOT_FOUND));

		PkLog pkLog = PkLog.builder()
			.pkType(type)
			.point(pkCriteria.getPoint())
			.user(user)
			.build();

		pkLogRepository.save(pkLog);
		userService.increaseUserPoint(user, pkCriteria.getPoint());

	}

}
