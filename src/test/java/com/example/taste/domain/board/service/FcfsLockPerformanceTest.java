package com.example.taste.domain.board.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;

@Tag("Performance")
@SpringBootTest
class FcfsLockPerformanceTest {

	@Test
	@Transactional
	void tryEnterFcfsQueue() {
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