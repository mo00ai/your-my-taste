package com.example.taste.domain.board.batch;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.taste.common.batch.BatchLauncherJob;

@Configuration
public class ClosingBoardQuartzConfig {
	@Bean
	public JobDetail closingBoardJobDetail() {
		return JobBuilder.newJob(BatchLauncherJob.class)
			.withIdentity("closingBoardJobDetail")
			.usingJobData("jobName", "closingBoardJob")
			.storeDurably()
			.build();
	}

	@Bean
	public Trigger closingBoardJobTrigger(JobDetail closingBoardJobDetail) {
		return TriggerBuilder.newTrigger()
			.forJob(closingBoardJobDetail)
			.withIdentity("closingBoardJobTrigger")
			.withSchedule(CronScheduleBuilder.cronSchedule("0 0/10 * * * ?"))
			.build();
	}
}
