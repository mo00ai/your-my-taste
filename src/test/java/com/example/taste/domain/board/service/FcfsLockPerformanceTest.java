package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.RedisConst.*;
import static com.example.taste.domain.board.entity.AccessPolicy.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.board.repository.FcfsInformationRepository;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.BoardFixture;
import com.example.taste.fixtures.CategoryFixture;
import com.example.taste.fixtures.ImageFixture;
import com.example.taste.fixtures.StoreFixture;
import com.example.taste.fixtures.UserFixture;
import com.example.taste.property.AbstractIntegrationTest;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.transaction.Transactional;

@Tag("Performance")
@SpringBootTest
class FcfsLockPerformanceTest extends AbstractIntegrationTest {

	//@MockitoBean
	//BoardStatusPublisher boardStatusPublisher;

	@Autowired
	MeterRegistry meterRegistry;
	@Autowired
	private FcfsInformationRepository fcfsInformationRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private FcfsQueueService fcfsQueueService;
	@Autowired
	private RedisService redisService;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	// @BeforeAll
	// public static void setUp() throws SQLException {
	// 	// 테스트용 DB 연결
	// 	Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/test_db", "test_user", "test_password");
	//
	// 	try (Statement stmt = connection.createStatement()) {
	// 		stmt.execute("CREATE EXTENSION IF NOT EXISTS vector;");
	// 	} catch (SQLException e) {
	// 		System.out.println("Error creating pgvector extension: " + e.getMessage());
	// 	}
	// }

	@Test
	void tryEnterFcfsQueue() {
		// given
		int threadCount = 102;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CyclicBarrier barrier = new CyclicBarrier(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		Image image1 = ImageFixture.create();
		User postingUser = userRepository.saveAndFlush(UserFixture.createNoMorePosting(image1));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.saveAndFlush(StoreFixture.create(category));
		Board board = boardRepository.saveAndFlush(
			BoardFixture.createOBoard("title", "contents", "O", FCFS.name(), 10,
				LocalDateTime.now(), store, postingUser));

		Timer.Sample methodTimer = Timer.start(meterRegistry);

		// when
		for (int i = 0; i < threadCount; i++) {
			Image image2 = ImageFixture.create();
			User user = userRepository.saveAndFlush(UserFixture.createNoMorePosting(image2));

			executorService.submit(() -> {
				try {
					barrier.await(); // 모든 쓰레드가 준비될 때까지 대기
					fcfsQueueService.tryEnterFcfsQueue(board, user);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}

		try {
			latch.await(); // 모든 작업 종료 대기
			executorService.shutdown();
			methodTimer.stop(meterRegistry.timer("test_time"));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		// then
		String key = FCFS_KEY_PREFIX + board.getId();

		//assertThat(fcfsInformationRepository.existsByBoardId(board.getId())).isTrue();
		//assertThat(redisService.getZSetRange(key)).isEmpty();
		assertThat(redisService.getZSetSize(key)).isEqualTo(0);

		// clean-up
		boardRepository.deleteAllInBatch();     // Board
		storeRepository.deleteAllInBatch();     // Store
		categoryRepository.deleteAllInBatch();  // Category
		userRepository.deleteAllInBatch();      // User
	}

	@Test
	@Transactional
	void tryEnterFcfsQueueByLettuce() {
	}

	@Test
	@Transactional
	void tryEnterFcfsQueueByRedisson() {
	}
}