package com.example.taste.domain.user.batch;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.taste.common.batch.BatchLauncherJob;

@Configuration
public class ResetPostCntQuartzConfig {
	@Bean
	public JobDetail resetPostCntJobDetail() {
		return JobBuilder.newJob(BatchLauncherJob.class)
			.withIdentity("resetPostCntJobDetail")
			.usingJobData("jobName", "resetPostCntJob") // 실제 Batch Job 이름
			.storeDurably() // Trigger 없어도 삭제되지 않음
			.build();
	}

	@Bean
	public Trigger resetPostCntJobTrigger(JobDetail resetPostCntJobDetail) {
		return TriggerBuilder.newTrigger()
			.forJob(resetPostCntJobDetail)
			.withIdentity("resetPostCntJobTrigger")
			.withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 1 * ?")) // 매월 1일 0시
			.build();
	}
}
