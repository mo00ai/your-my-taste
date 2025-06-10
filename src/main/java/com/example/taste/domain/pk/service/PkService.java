package com.example.taste.domain.pk.service;

import static com.example.taste.domain.pk.exception.PkErrorCode.DUPLICATE_PK_TYPE;
import static com.example.taste.domain.pk.exception.PkErrorCode.PK_CRITERIA_NOT_FOUND;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.pk.dto.request.PkLogCacheDto;
import com.example.taste.domain.pk.dto.request.PkUpdateRequestDto;
import com.example.taste.domain.pk.dto.response.PkCriteriaResponseDto;
import com.example.taste.domain.pk.entity.PkCriteria;
import com.example.taste.domain.pk.entity.PkLog;
import com.example.taste.domain.pk.entity.PkTerm;
import com.example.taste.domain.pk.entity.PkTermRanking;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.repository.PkCriteriaRepository;
import com.example.taste.domain.pk.repository.PkLogJdbcRepository;
import com.example.taste.domain.pk.repository.PkLogRepository;
import com.example.taste.domain.pk.repository.PkTermRankingRepository;
import com.example.taste.domain.pk.repository.PkTermRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.domain.user.service.UserService;

@Service
@RequiredArgsConstructor
public class PkService {
	private final EntityFetcher entityFetcher;
	private final PkCriteriaRepository pkCriteriaRepository;
	private final PkLogRepository pkLogRepository;
	private final UserRepository userRepository;
	private final UserService userService;
	private final RedisService redisService;
	private final PkCacheService pkCacheService;
	private final PkTermRepository pkTermRepository;
	private final PkTermRankingRepository pkTermRankingRepository;
	private final PkLogJdbcRepository pkLogJdbcRepository;

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

	@Transactional(readOnly = true)
	public List<PkCriteriaResponseDto> findAllPkCriteria() {

		return pkCacheService.findAllPkCriteria();

	}

	@CacheEvict(value = "pkCriteriaCache", allEntries = true)
	@Transactional
	public void updatePkCriteria(Long id, PkUpdateRequestDto dto) {

		PkCriteria pkCriteria = entityFetcher.getPkCriteriaOrThrow(id);

		pkCriteria.update(dto);

	}

	@CacheEvict(value = "pkCriteriaCache", allEntries = true)
	@Transactional
	public void deletePkCriteria(Long id) {

		PkCriteria pkCriteria = entityFetcher.getPkCriteriaOrThrow(id);
		pkCriteria.delete();

	}

	@Transactional
	public void savePkLog(Long userId, PkType type) {

		User user = entityFetcher.getUserOrThrow(userId);

		int point = getPointByPkType(type);

		PkLogCacheDto dto = PkLogCacheDto.builder()
			.userId(user.getId())
			.pkType(type)
			.point(point)
			.createdAt(LocalDateTime.now())
			.build();

		String key = "pkLog:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ":" + user.getId();
		redisService.setOpsForList(key, dto, Duration.ofDays(1));

		userService.increaseUserPoint(user, point);

	}

	public int getPointByPkType(PkType type) {

		List<PkCriteriaResponseDto> criteriaList = pkCacheService.findAllPkCriteria();

		return criteriaList.stream()
			.filter(dto -> dto.getType().equals(type.name()))
			.map(PkCriteriaResponseDto::getPoint)
			.findFirst()
			.orElseThrow(() -> new CustomException(PK_CRITERIA_NOT_FOUND));

	}

	@Transactional
	public void runPkTermRankingScheduler(LocalDate now) {
		PkTerm term = savePkTerm(now);
		savePkTermRankingUsers(term);
		userService.resetUsersPoint();
	}

	@Transactional
	public PkTerm savePkTerm(LocalDate now) {

		int term = getMaxPkTerm();

		LocalDateTime startDateTime = now.withDayOfMonth(1).atStartOfDay();
		LocalDateTime endDateTime = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);

		PkTerm pkTerm = PkTerm.builder()
			.term(term)
			.startDate(startDateTime)
			.endDate(endDateTime)
			.build();

		PkTerm savedPkTerm = pkTermRepository.save(pkTerm);

		return savedPkTerm;
	}

	//PkTermRanking에 집어넣기
	@Transactional
	public void savePkTermRankingUsers(PkTerm term) {
		List<User> pkRankingUsers = userService.findPkRankingUsers();

		if (pkRankingUsers == null || pkRankingUsers.isEmpty()) {
			throw new CustomException(PK_RANKERS_NOT_EXIST);
		}

		List<PkTermRanking> rankings = new ArrayList<>();

		for (int i = 0; i < pkRankingUsers.size(); i++) {
			User user = pkRankingUsers.get(i);
			int rank = i + 1;
			int point = user.getPoint();

			PkTermRanking ranking = PkTermRanking.builder()
				.ranking(rank)
				.point(point)
				.pkTerm(term)
				.user(user)
				.build();

			rankings.add(ranking);
		}

		pkTermRankingRepository.saveAll(rankings);

	}

	private int getMaxPkTerm() {
		return pkTermRepository.findTopByOrderByTermDesc()
			.map(pkTerm -> pkTerm.getTerm() + 1)
			.orElse(1);
	}

	@Transactional
	public void saveBulkPkLogs(List<PkLog> pkLogs) {
		pkLogJdbcRepository.batchInsert(pkLogs);
	}
}
