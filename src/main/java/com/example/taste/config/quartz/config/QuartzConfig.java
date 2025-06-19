package com.example.taste.config.quartz.config;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.taste.config.quartz.job.QuartzBatchLauncherJob;

@Configuration
public class QuartzConfig {

	@Bean
	public JobDetail jobDetail() {
		return JobBuilder.newJob(QuartzBatchLauncherJob.class)
			.withIdentity("testJobDetail")
			.storeDurably()
			.build();
	}

	@Bean
	public Trigger trigger(JobDetail jobDetail) {
		return TriggerBuilder.newTrigger()
			.forJob(jobDetail)
			.withIdentity("testTrigger")
			.withSchedule(SimpleScheduleBuilder.simpleSchedule()
				.withIntervalInSeconds(10)
				.repeatForever())
			.build();
	}
}
