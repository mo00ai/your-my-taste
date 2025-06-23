package com.example.taste.domain.event.batch;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class EventWinnerBatchConfig extends DefaultBatchConfiguration {
	private final DataSource dataSource;
	private final PlatformTransactionManager transactionManager;

	@Override
	protected DataSource getDataSource() {
		return dataSource;
	}

	@Override
	protected PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	//재시도 로직
	@Bean
	public Job EventWinnerJob(JobRepository repo, Step step) {
		return new JobBuilder("winnerSelectionJob", repo)
			.start(step)
			.build();
	}

	@Bean
	public Step step(JobRepository repo, PlatformTransactionManager tx) {
		return new StepBuilder("winnerStep", repo)
			.<String, String>chunk(10, tx)
			.reader(reader())
			.processor(processor())
			.writer(writer())
			.build();
	}

	@Bean
	public ItemReader<String> reader() {

		return new ListItemReader<>(List.of("EVENT1", "EVENT2"));
	}

	@Bean
	public ItemProcessor<String, String> processor() {

		return item -> item.toLowerCase();
	}

	@Bean
	public ItemWriter<String> writer() {

		return items -> items.forEach(System.out::println);
	}

	//스텝의 하위 단계
	//스텝을 만들면 스텝 하나에 테스크렛 하나가 만드러짐
	// @Bean
	// public Tasklet myTasklet() {
	// 	return (contribution, context) -> {
	// 		// 여기 안에 로직을 다 씀
	// 		System.out.println("DB 조회 → 조건 체크 → 포인트 지급");
	// 		return RepeatStatus.FINISHED;
	// 	};
	// }

}
