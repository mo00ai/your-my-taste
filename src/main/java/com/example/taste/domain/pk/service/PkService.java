package com.example.taste.domain.pk.service;

import static com.example.taste.domain.pk.exception.PkErrorCode.*;
import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.pk.dto.request.PkLogCacheDto;
import com.example.taste.domain.pk.entity.PkLog;
import com.example.taste.domain.pk.entity.PkTerm;
import com.example.taste.domain.pk.entity.PkTermRanking;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.repository.PkLogRepository;
import com.example.taste.domain.pk.repository.PkTermRankingRepository;
import com.example.taste.domain.pk.repository.PkTermRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PkService {
	private final PkLogRepository pkLogRepository;
	private final UserRepository userRepository;
	private final UserService userService;
	private final RedisService redisService;
	private final PkTermRepository pkTermRepository;
	private final PkTermRankingRepository pkTermRankingRepository;

	@Transactional
	public void savePkLog(Long userId, PkType type) {

		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_FOUND_USER));

		PkLogCacheDto dto = PkLogCacheDto.builder()
			.userId(user.getId())
			.pkType(type)
			.point(type.getPoint())
			.createdAt(LocalDateTime.now())
			.build();

		String key = "pkLog:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ":" + user.getId();
		redisService.setOpsForList(key, dto, Duration.ofDays(1));

		userService.increaseUserPoint(user, type.getPoint());

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

	// PkTermRanking에 집어넣기
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
		//jcbc
		// pkLogJdbcRepository.batchInsert(pkLogs);

		//jpa
		// pkLogRepository.saveAll(pkLogs);

		pkLogRepository.insertPkLogs(pkLogs);
	}
}
