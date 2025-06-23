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
public class PkLogQuartzConfig {

	@Bean
	public JobDetail pkLogJobDetail() {
		return JobBuilder.newJob(BatchLauncherJob.class)
			.withIdentity("PkLogBatchJobDetail")
			.usingJobData("jobName", "PkLogBatchJob")
			.storeDurably()
			.build();
	}

	@Bean
	public Trigger pkLogJobTrigger(JobDetail pkLogJobDetail) {
		return TriggerBuilder.newTrigger()
			.forJob(pkLogJobDetail)
			.withIdentity("PkLogBatchTrigger")
			.withPriority(10)
			.withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?")) // 매일 자정
			// .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?"))
			.build();
	}
}
