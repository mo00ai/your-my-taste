package com.example.taste.common.batchquartz.config;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.taste.common.batchquartz.quartz.job.QuartzBatchLauncherJob;

@Configuration
public class QuartzConfig {

	@Bean
	public JobDetail testJobDetail() {
		return JobBuilder.newJob(QuartzBatchLauncherJob.class)
			.withIdentity("testJobDetail")
			.usingJobData("jobName", "jobName") // BatchConfig에서 정의한 Job 이름과 동일해야 함
			.storeDurably()
			.build();
	}

	@Bean
	public Trigger testJobTrigger(JobDetail testJobDetail) {
		return TriggerBuilder.newTrigger()
			.forJob(testJobDetail)
			.withIdentity("testJobTrigger")
			.withSchedule(SimpleScheduleBuilder.simpleSchedule()
				.withIntervalInSeconds(30) // 30초마다 실행 예시
				.repeatForever())
			.build();
	}
}
