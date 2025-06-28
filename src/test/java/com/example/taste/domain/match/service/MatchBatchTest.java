package com.example.taste.domain.match.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.taste.TasteApplication;
import com.example.taste.domain.match.config.TestBatchConfig;
import com.example.taste.domain.party.batch.PartyBatchConfig;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.party.repository.PartyRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.PartyFixture;
import com.example.taste.fixtures.UserFixture;
import com.example.taste.property.AbstractIntegrationTest;

@ActiveProfiles("local")
@ExtendWith(SpringExtension.class)
@SpringBatchTest
@SpringBootTest(classes = {TasteApplication.class, TestBatchConfig.class, PartyBatchConfig.class})
public class MatchBatchTest extends AbstractIntegrationTest {
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PartyRepository partyRepository;

	@Autowired
	@Qualifier("updatePartyJob")
	private Job partyJob;

	@BeforeEach
	void setUpJob() {
		jobLauncherTestUtils.setJob(partyJob);
	}

	@Test
	@DisplayName("파티 만료/삭제 배치가 동작한다")
	public void runPartyBatchTest() throws Exception {
		// given, when : 배치 실행 시
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();

		// then
		assertThat(jobExecution.getExitStatus())
			.as("배치 잡이 정상 완료 상태여야 한다")
			.isEqualTo(ExitStatus.COMPLETED);
	}

	@Test
	@DisplayName("배치 스텝의 파티 만료/삭제가 동작한다")
	public void batchExpireAndSoftDeletePartyTest() throws Exception {
		// given : 만료 / 삭제 대상 파티가 있을 때 (모임 날짜가 각각 오늘로부터 -1, -7)
		User user = userRepository.save(UserFixture.create(null));
		Party expireParty = PartyFixture.createWithDate(
			user, LocalDate.now().minusDays(1));
		Party softDeleteParty = PartyFixture.createExpiredParty(
			user, LocalDate.now().minusDays(7));

		partyRepository.save(expireParty);
		partyRepository.save(softDeleteParty);

		// when
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		assertThat(jobExecution.getExitStatus())
			.as("배치 잡이 정상 완료 상태여야 한다")
			.isEqualTo(ExitStatus.COMPLETED);

		// then
		Party updatedExpireParty = partyRepository.findById(expireParty.getId())
			.orElseThrow();
		Party updatedSoftDeleteParty = partyRepository.findById(softDeleteParty.getId())
			.orElseThrow();

		assertThat(updatedExpireParty.getPartyStatus())
			.as("모임 날짜로부터 1일 지난 파티는 EXPIRED 상태여야 한다")
			.isEqualTo(PartyStatus.EXPIRED);
		assertThat(updatedSoftDeleteParty.getDeletedAt())
			.as("모임 날짜로부터 7일 지난 파티는 deleteAt 값이 설정 되어야 한다")
			.isNotNull();

		cleanUp();
	}

	private void cleanUp() {
		userRepository.deleteAllInBatch();
		partyRepository.deleteAllInBatch();
	}
}
