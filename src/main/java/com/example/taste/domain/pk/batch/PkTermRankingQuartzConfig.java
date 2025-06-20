package com.example.taste.domain.pk.batch;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.taste.common.batch.BatchLauncherJob;

@Configuration
public class PkTermRankingQuartzConfig {
	@Bean
	public JobDetail pkTermJobDetail() {
		return JobBuilder.newJob(BatchLauncherJob.class)
			.withIdentity("pkTermJobDetail")
			.usingJobData("jobName", "pkTermRankingJob")
			.storeDurably()
			.build();
	}

	@Bean
	public Trigger pkTermTrigger(JobDetail pkTermJobDetail) {
		return TriggerBuilder.newTrigger()
			.forJob(pkTermJobDetail)
			.withIdentity("pkTermRankingTrigger")
			.withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 1 * ?")) // 매월 1일 00시
			.build();
	}
}
