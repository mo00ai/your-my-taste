package com.example.taste.domain.pk.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.pk.entity.PkTerm;
import com.example.taste.domain.pk.entity.PkTermRanking;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.repository.PkLogRepository;
import com.example.taste.domain.pk.repository.PkTermRankingRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.UserFixture;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@ActiveProfiles("test-int")
class PkServiceTest {

	@Autowired
	private PkService pkService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PkLogRepository pkLogRepository;
	@Autowired
	private PkTermRankingRepository pkTermRankingRepository;
	@Autowired
	private RedisService redisService;
	@Autowired
	private EntityManager em;

	@BeforeEach
	void setup() {

	}

	@Test
	void savePkLog_saveToRedis() {
		// // given
		// User user = userRepository.save(UserFixture.create(ImageFixture.create()));
		//
		// // when
		// pkService.savePkLog(user.getId(), PkType.POST);
		// em.flush();
		// em.refresh(user);
		//
		// // then
		// assertThat(user.getPoint()).isEqualTo(30);
		// String key = "pkLog:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ":" + user.getId();
		// List<Object> logs = redisService.getOpsForList(key, Object.class);
		// assertThat(logs).hasSize(1);
	}

	@Test
	void runPkTermRankingScheduler_ResetUsersPoint() {
		// given
		List<User> users = userRepository.saveAll(UserFixture.createUsers());
		pkService.savePkLog(users.get(0).getId(), PkType.POST);

		// when
		pkService.runPkTermRankingScheduler(LocalDate.of(2025, 6, 1));
		em.flush();
		em.clear();

		// then
		List<PkTermRanking> rankings = pkTermRankingRepository.findAll();
		assertThat(rankings).hasSize(10);
		assertThat(rankings.get(0).getUser().getId()).isEqualTo(users.get(0).getId());

		User refreshed = userRepository.findById(users.get(0).getId()).orElseThrow();
		assertThat(refreshed.getPoint()).isZero();
	}

	@Test
	void savePkTerm_success() {
		// given
		LocalDate termStart = LocalDate.of(2025, 6, 1);

		// when
		PkTerm term = pkService.savePkTerm(termStart);

		// then
		assertThat(term.getStartDate().toLocalDate()).isEqualTo(termStart);
		assertThat(term.getEndDate().toLocalDate()).isEqualTo(LocalDate.of(2025, 6, 30));
	}

}
