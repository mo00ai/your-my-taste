package com.example.taste.domain.pk.service;

import static com.example.taste.domain.pk.exception.PkErrorCode.*;
import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.pk.dto.request.PkLogCacheDto;
import com.example.taste.domain.pk.dto.request.PkUpdateRequestDto;
import com.example.taste.domain.pk.dto.response.PkCriteriaResponseDto;
import com.example.taste.domain.pk.entity.PkCriteria;
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
	private final RedisService redisService;
	private final PkCacheService pkCacheService;

	@CacheEvict(value = "pkCriteriaCache", allEntries = true)
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

	@Cacheable(value = "pkCriteriaCache", key = "'all'", cacheManager = "redisCacheManager")
	@Transactional(readOnly = true)
	public List<PkCriteriaResponseDto> findAllPkCriteria() {

		// List<PkCriteria> all = pkCriteriaRepository.findAll();
		//
		// return all.stream().map(pk -> PkCriteriaResponseDto.builder()
		// 		.id(pk.getId())
		// 		.type(pk.getType().name())
		// 		.point(pk.getPoint())
		// 		.isActive(pk.isActive())
		// 		.build()
		// 	)
		// 	.collect(Collectors.toList());

		return pkCacheService.findAllPkCriteria();

	}

	@CacheEvict(value = "pkCriteriaCache", allEntries = true)
	@Transactional
	public void updatePkCriteria(Long id, PkUpdateRequestDto dto) {

		PkCriteria pkCriteria = pkCriteriaRepository.findById(id)
			.orElseThrow(() -> new CustomException(PK_CRITERIA_NOT_FOUND));

		pkCriteria.update(dto);

	}

	@CacheEvict(value = "pkCriteriaCache", allEntries = true)
	@Transactional
	public void deletePkCriteria(Long id) {

		PkCriteria pkCriteria = pkCriteriaRepository.findById(id)
			.orElseThrow(() -> new CustomException(PK_CRITERIA_NOT_FOUND));

		pkCriteria.delete();

	}

	// @Transactional
	// public void savePkLog(Long userId, PkType type) {
	//
	// 	User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
	//
	// 	int point = getPointByPkType(type);
	//
	// 	PkCriteria pkCriteria = pkCriteriaRepository.findByType(type)
	// 		.orElseThrow(() -> new CustomException(PK_CRITERIA_NOT_FOUND));
	//
	// 	PkLog pkLog = PkLog.builder()
	// 		.pkType(type)
	// 		.point(pkCriteria.getPoint())
	// 		.user(user)
	// 		.build();
	//
	// 	pkLogRepository.save(pkLog);
	// 	userService.increaseUserPoint(user, pkCriteria.getPoint());
	//
	// }

	@Transactional
	public void savePkLog(Long userId, PkType type) {

		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

		int point = getPointByPkType(type);

		PkLogCacheDto dto = PkLogCacheDto.builder()
			.userId(user.getId())
			.pkType(type)
			.point(point)
			.createdAt(LocalDateTime.now())
			.build();

		String key = "pkLog:" + LocalDate.now() + ":" + user.getId();
		redisService.setOpsForList(key, dto, Duration.ofDays(1));

		userService.increaseUserPoint(user, point);

	}

	@SuppressWarnings("unchecked")
	public int getPointByPkType(PkType type) {

		// Object beforeCasingDto = redisService.getKeyValue("pkCriteriaCache::all");

		// 캐시에 없으면 DB에서 조회 후 캐시에 저장하는 안전장치 로직
		// if (beforeCasingDto == null) {
		// 	List<PkCriteria> all = pkCriteriaRepository.findAll();
		//
		// 	if (all.isEmpty()) {
		// 		throw new CustomException(PK_CRITERIA_NOT_FOUND);
		// 	}
		//
		// 	List<PkCriteriaResponseDto> dtoList = all.stream()
		// 		.map(pk -> PkCriteriaResponseDto.builder()
		// 			.id(pk.getId())
		// 			.type(pk.getType().name())
		// 			.point(pk.getPoint())
		// 			.isActive(pk.isActive())
		// 			.build()
		// 		).collect(Collectors.toList());
		//
		// 	redisService.setKeyValue("pkCriteriaCache::all", dtoList);
		// 	beforeCasingDto = dtoList;
		//
		// }

		List<PkCriteriaResponseDto> criteriaList = pkCacheService.findAllPkCriteria();

		// if (!(beforeCasingDto instanceof List<?> list)) {
		// 	throw new CustomException(PK_CRITERIA_NOT_FOUND);
		// }
		//
		// if (!list.isEmpty() && !(list.get(0) instanceof PkCriteriaResponseDto)) {
		// 	throw new CustomException(PK_CRITERIA_NOT_FOUND);
		// }
		//
		// List<PkCriteriaResponseDto> castedDto = (List<PkCriteriaResponseDto>)beforeCasingDto;

		return criteriaList.stream()
			.filter(dto -> dto.getType().equals(type.name()))
			.map(PkCriteriaResponseDto::getPoint)
			.findFirst()
			.orElseThrow(() -> new CustomException(PK_CRITERIA_NOT_FOUND));

	}

}
