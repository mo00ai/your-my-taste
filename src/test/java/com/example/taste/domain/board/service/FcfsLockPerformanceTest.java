package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.RedisConst.*;
import static com.example.taste.domain.board.entity.AccessPolicy.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.board.repository.FcfsInformationRepository;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.repository.ImageRepository;
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
	@Autowired
	private ImageRepository imageRepository;

	@Test
	@Transactional
	void tryEnterFcfsQueue() {
		// given
		int threadCount = 100;
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
		System.out.println("board id = " + board.getId());
		BoardResponseDto dto = new BoardResponseDto(board);

		List<User> createdUsers = Collections.synchronizedList(new ArrayList<>());
		List<Image> createdImages = Collections.synchronizedList(new ArrayList<>());

		Timer.Sample methodTimer = Timer.start(meterRegistry);

		// when
		for (int i = 0; i < threadCount; i++) {
			Image image2 = ImageFixture.create();
			User user = userRepository.saveAndFlush(UserFixture.createNoMorePosting(image2));
			createdUsers.add(user);
			createdImages.add(image2);

			executorService.submit(() -> {
				try {
					barrier.await(); // 모든 쓰레드가 준비될 때까지 대기
					fcfsQueueService.tryEnterFcfsQueue(dto, user);
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
			methodTimer.stop(meterRegistry.timer("base_test_time"));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		// then
		String key = FCFS_KEY_PREFIX + board.getId();

		assertThat(fcfsInformationRepository.existsByBoardId(board.getId())).isTrue();
		assertThat(redisService.getZSetRange(key)).isEmpty();
		assertThat(redisService.getZSetSize(key)).isEqualTo(0);

		// clean-up
		fcfsInformationRepository.deleteAll(fcfsInformationRepository.findAllByBoardId(board.getId()));
		boardRepository.deleteById(board.getId());
		storeRepository.deleteById(store.getId());
		categoryRepository.deleteById(category.getId());
		userRepository.deleteAll(createdUsers);
		userRepository.deleteById(postingUser.getId());
		imageRepository.deleteAll(createdImages);
		imageRepository.deleteById(image1.getId());
	}

	@Test
	@Transactional
	void tryEnterFcfsQueueByLettuce() {
		// given
		int threadCount = 100;
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
		System.out.println("board id = " + board.getId());
		BoardResponseDto dto = new BoardResponseDto(board);

		List<User> createdUsers = Collections.synchronizedList(new ArrayList<>());
		List<Image> createdImages = Collections.synchronizedList(new ArrayList<>());

		Timer.Sample methodTimer = Timer.start(meterRegistry);

		// when
		for (int i = 0; i < threadCount; i++) {
			Image image2 = ImageFixture.create();
			User user = userRepository.saveAndFlush(UserFixture.createNoMorePosting(image2));
			createdUsers.add(user);
			createdImages.add(image2);

			executorService.submit(() -> {
				try {
					barrier.await(); // 모든 쓰레드가 준비될 때까지 대기
					fcfsQueueService.tryEnterFcfsQueueByLettuce(dto, user);
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
			methodTimer.stop(meterRegistry.timer("lettuce_test_time"));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		// then
		String key = FCFS_KEY_PREFIX + board.getId();

		assertThat(fcfsInformationRepository.existsByBoardId(board.getId())).isTrue();
		assertThat(redisService.getZSetRange(key)).isEmpty();
		assertThat(redisService.getZSetSize(key)).isEqualTo(0);

		// clean-up
		fcfsInformationRepository.deleteAll(fcfsInformationRepository.findAllByBoardId(board.getId()));
		boardRepository.deleteById(board.getId());
		storeRepository.deleteById(store.getId());
		categoryRepository.deleteById(category.getId());
		userRepository.deleteAll(createdUsers);
		userRepository.deleteById(postingUser.getId());
		imageRepository.deleteAll(createdImages);
		imageRepository.deleteById(image1.getId());
	}

	@Test
	@Transactional
	void tryEnterFcfsQueueByRedisson() {
		// given
		int threadCount = 100;
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
		System.out.println("board id = " + board.getId());
		BoardResponseDto dto = new BoardResponseDto(board);

		List<User> createdUsers = Collections.synchronizedList(new ArrayList<>());
		List<Image> createdImages = Collections.synchronizedList(new ArrayList<>());

		Timer.Sample methodTimer = Timer.start(meterRegistry);

		// when
		for (int i = 0; i < threadCount; i++) {
			Image image2 = ImageFixture.create();
			User user = userRepository.saveAndFlush(UserFixture.createNoMorePosting(image2));
			createdUsers.add(user);
			createdImages.add(image2);

			executorService.submit(() -> {
				try {
					barrier.await(); // 모든 쓰레드가 준비될 때까지 대기
					fcfsQueueService.tryEnterFcfsQueueByRedisson(dto, user);
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
			methodTimer.stop(meterRegistry.timer("redisson_test_time"));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		// then
		String key = FCFS_KEY_PREFIX + board.getId();

		assertThat(fcfsInformationRepository.existsByBoardId(board.getId())).isTrue();
		assertThat(redisService.getZSetRange(key)).isEmpty();
		assertThat(redisService.getZSetSize(key)).isEqualTo(0);

		// clean-up
		fcfsInformationRepository.deleteAll(fcfsInformationRepository.findAllByBoardId(board.getId()));
		boardRepository.deleteById(board.getId());
		storeRepository.deleteById(store.getId());
		categoryRepository.deleteById(category.getId());
		userRepository.deleteAll(createdUsers);
		userRepository.deleteById(postingUser.getId());
		imageRepository.deleteAll(createdImages);
		imageRepository.deleteById(image1.getId());
	}
}