package com.example.taste.domain.user.batch;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.taste.domain.user.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ResetPostCntBatchConfig extends DefaultBatchConfiguration {

	@Bean
	public Job resetPostCntJob(JobRepository repo, Step resetPostCntStep) {
		return new JobBuilder("resetPostCntJob", repo)
			.start(resetPostCntStep)
			.build();
	}

	@Bean
	@JobScope
	public Step resetPostCntStep(JobRepository repo, PlatformTransactionManager tm, Tasklet resetPostCntTasklet) {
		return new StepBuilder("resetPostCntStep", repo)
			.tasklet(resetPostCntTasklet, tm) // tm 적용해야 재시작, 롤백 정책 적용 가능
			.build();
	}

	@Bean
	@StepScope
	public Tasklet resetPostCntTasklet(UserService userService) {
		return (contribution, chunkContext) -> {
			int retryCount = 0;
			while (retryCount < 3) { // tasklet은 재시도 로직 직접 구현 필요
				try {
					long updatedCount = userService.resetPostingCnt();
					log.info("[UserScheduler] 총 {}명 유저 포스팅 횟수 초기화 완료", updatedCount);
					return RepeatStatus.FINISHED;
				} catch (DataAccessException e) {
					log.info("[UserScheduler] 포스팅 횟수 초기화 실패. 시도 횟수: {}/3", retryCount + 1, e);
					retryCount++;
				}
			}

			log.error("[UserScheduler] 포스팅 횟수 초기화 실패 - 재시도 횟수 초과");
			contribution.setExitStatus(ExitStatus.FAILED); // 배치 실패 상태로 기록
			return RepeatStatus.FINISHED;
		};
	}
}
