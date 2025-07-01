package com.example.taste.domain.match.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.match.dto.MatchEvent;
import com.example.taste.domain.match.dto.PartyMatchInfoDto;
import com.example.taste.domain.match.entity.PartyMatchInfo;
import com.example.taste.domain.match.enums.MatchJobType;
import com.example.taste.domain.match.redis.MatchPublisher;
import com.example.taste.domain.match.repository.PartyMatchInfoRepository;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.repository.PartyInvitationRepository;

@Tag("Performance")
@SpringBootTest
public class MatchServicePerformanceTest {
	@Autowired
	private RedisService redisService;

	@Autowired
	private PartyMatchInfoRepository partyMatchInfoRepository;

	@Autowired
	private PartyInvitationRepository partyInvitationRepository;

	@Autowired
	private MatchEngineService matchEngineService;

	@Autowired
	private MatchEngineCacheService matchEngineCacheService;

	@Autowired
	private MatchPublisher matchPublisher;

	@Test
	@Transactional
	@Rollback(false)
	@DisplayName("파티 매칭 캐시 등록")
	public void cachePartyMatchInfoTest() {
		List<PartyMatchInfo> partyMatchInfoList
			= partyMatchInfoRepository.findAll();

		for (PartyMatchInfo partyMatchInfo : partyMatchInfoList) {
			// 캐싱 적용
			String key = "partyMatchInfo" + partyMatchInfo.getId();
			long ttlDays = ChronoUnit.DAYS.between(LocalDate.now(), partyMatchInfo.getMeetingDate()) + 1;
			List<PartyInvitation> partyInvitationList
				= partyInvitationRepository.findByPartyId(partyMatchInfo.getParty().getId());
			Double avgAge = partyMatchInfo.getParty().calculateAverageMemberAge(partyInvitationList);
			PartyMatchInfoDto matchInfoDto = new PartyMatchInfoDto(partyMatchInfo, avgAge);

			if (ttlDays > 0) {
				redisService.setKeyValue(key, matchInfoDto, Duration.ofDays(ttlDays));
			}

			System.out.println("등록 대상 Key = " + key);
			System.out.println("등록 대상 TTL = " + ttlDays);
			System.out.println("등록 대상 DTO = " + matchInfoDto);
			Object cached = redisService.getKeyValue("partyMatchInfo" + partyMatchInfo.getId());
			System.out.println("Redis 조회 결과: " + cached);
		}
	}

	@Test
	@Transactional
	@Rollback(value = true)
	@DisplayName("파티 매칭 엔진 동작 시간 측정")
	public void runPartyMatchEngineTest() {
		long start = System.nanoTime();

		matchEngineService.runMatchingForUser(List.of(1L));

		long end = System.nanoTime();

		System.out.println("파티 매칭 실행 시간: " + (end - start) / 1_000_000.0 + " ms");
	}

	@Test
	@Transactional
	@Rollback(value = true)
	@DisplayName("파티 매칭 병렬 500건 테스트")
	public void runPartyMatchEngineParallelTest() throws InterruptedException {
		int taskCount = 500;
		ExecutorService executor = Executors.newFixedThreadPool(50); // 적절한 스레드 풀 크기 지정
		CountDownLatch latch = new CountDownLatch(taskCount);

		try {
			long start = System.nanoTime();

			for (long i = 1; i <= taskCount; i++) {
				long userId = i;
				executor.submit(() -> {
					try {
						matchEngineService.runMatchingForUser(List.of(userId));
					} finally {
						latch.countDown();
					}
				});
			}

			latch.await(); // 모든 작업 완료 대기
			long end = System.nanoTime();
			System.out.println("파티 매칭 병렬 500건 실행 시간: " + (end - start) / 1_000_000.0 + " ms");
		} finally {
			executor.shutdown();
		}
	}

	@Test
	@Transactional
	@DisplayName("파티 매칭 캐시 엔진 동작 시간 측정")
	public void runPartyMatchEngineCacheTest() {
		long start = System.nanoTime();

		matchEngineCacheService.runMatchingForUser(List.of(1L));

		long end = System.nanoTime();

		System.out.println("파티 매칭 캐싱 실행 시간: " + (end - start) / 1_000_000.0 + " ms");
	}

	@Test
	@Transactional
	@DisplayName("파티 매칭 캐시 병렬 500건 테스트")
	public void runPartyMatchEngineCacheParallelTest() throws InterruptedException {
		int taskCount = 500;
		ExecutorService executor = Executors.newFixedThreadPool(50); // 시스템 자원에 따라 조정
		CountDownLatch latch = new CountDownLatch(taskCount);

		try {
			long start = System.nanoTime();

			for (long i = 1; i <= taskCount; i++) {
				long userId = i;
				executor.submit(() -> {
					try {
						matchEngineCacheService.runMatchingForUser(List.of(userId));
					} finally {
						latch.countDown();
					}
				});
			}

			latch.await();
			long end = System.nanoTime();

			System.out.println("파티 매칭 캐싱 병렬 500건 실행 시간: " + (end - start) / 1_000_000.0 + " ms");
		} finally {
			executor.shutdown();
		}
	}

	// @BeforeAll
	// public static void dummyUserAndRandomMatchPartyBulkInsert() {
	// 	String INSERT_SQL =
	// 		"INSERT INTO party (host_user_id, title, party_status, meeting_date, max_members, now_members, enable_random_matching)"
	// 			+
	// 			" VALUES (?, ?, ?, ?, ?, ?, ?)";
	// 	int BATCH_SIZE = 1000;
	// 	int TOTAL = BATCH_SIZE;
	// 	User user = UserFixture.create(null);
	//
	// 	// 더미 유저 생성
	// 	for (int i = 0; i < TOTAL / BATCH_SIZE; i++) {
	// 		System.out.println("NOW BATCH : " + i);
	// 		jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
	// 			@Override
	// 			public void setValues(PreparedStatement ps, int i) throws SQLException {
	// 				ps.setLong(1, user.getId());
	// 				ps.setString(2, "Party" + UUID.randomUUID());
	// 				ps.setString(3, PartyStatus.ACTIVE.toString());
	// 				ps.setDate(4, Date.valueOf(LocalDate.now()));
	// 				ps.setInt(5, 5);
	// 				ps.setInt(6, 1);
	// 				ps.setBoolean(7, true);
	// 			}
	//
	// 			@Override
	// 			public int getBatchSize() {
	// 				return BATCH_SIZE;
	// 			}
	// 		});
	// 	}
	// }

	@Test
	@DisplayName("배치 레디스 메세지 100,000 건 부하 테스트")
	void publishMatchMessagePerformanceTest() {
		int count = 100000;
		int threadCount = 10;
		int perThreadCount = count / threadCount;

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		long start = System.currentTimeMillis();

		for (int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				for (long j = 0; j < perThreadCount; j++) {
					MatchEvent event = new MatchEvent(MatchJobType.USER_MATCH, List.of(j));
					matchPublisher.publish(event);
				}
				latch.countDown();
			});
		}

		try {
			latch.await();  // 모든 스레드 작업 완료 대기
		} catch (InterruptedException e) {
			System.out.println("예외 발생: " + e.getMessage());
		}
		long end = System.currentTimeMillis();
		double seconds = (end - start) / 1000.0;
		double tps = count / seconds;

		executor.shutdown();
		System.out.printf("총 %d개 발행(병렬 %d개 스레드), 소요시간: %.2f초, TPS: %.2f%n", count, threadCount, seconds, tps);
	}
}