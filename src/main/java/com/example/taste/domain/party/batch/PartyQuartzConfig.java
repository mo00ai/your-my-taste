package com.example.taste.domain.party.batch;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.taste.common.batch.BatchLauncherJob;

@Configuration
public class PartyQuartzConfig {
	@Bean
	public JobDetail updateExpiredPartyJobDetail() {
		return JobBuilder.newJob(BatchLauncherJob.class)
			.withIdentity("UpdatePartyBatchJobDetail")
			.usingJobData("jobName", "UpdatePartyBatchJob")
			.storeDurably()
			.build();
	}

	@Bean
	public Trigger updateExpiredPartyJobTrigger(JobDetail updateExpiredPartyJobDetail) {
		return TriggerBuilder.newTrigger()
			.forJob(updateExpiredPartyJobDetail)
			.withIdentity("UpdatePartyBatchTrigger")
			.withPriority(10)
			.withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?")) // 매일 자정마다
			.build();
	}
}
