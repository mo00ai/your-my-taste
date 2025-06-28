package com.example.taste.domain.match.service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.example.taste.domain.match.dto.MatchEvent;
import com.example.taste.domain.match.enums.MatchJobType;
import com.example.taste.domain.match.redis.MatchPublisher;
import com.example.taste.domain.party.repository.PartyRepository;

@Tag("Performance")
@ActiveProfiles("local")
@SpringBootTest
public class MatchServicePerformanceTest {
	@Autowired
	private static JdbcTemplate jdbcTemplate;

	@Autowired
	private static PartyRepository partyRepository;

	@Autowired
	private MatchPublisher matchPublisher;

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