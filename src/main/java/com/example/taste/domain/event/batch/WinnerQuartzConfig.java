package com.example.taste.domain.event.batch;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.taste.common.batch.BatchLauncherJob;

@Configuration
public class WinnerQuartzConfig {

	//여기다가도 우선순위 설정 가능
	@Bean
	public JobDetail winnerJobDetail() {
		return JobBuilder.newJob(BatchLauncherJob.class)
			.withIdentity("winnerJobDetail")
			.usingJobData("jobName", "winnerSelectionJob") // 실제 Batch Job 이름
			.storeDurably()
			.build();
	}

	//시간 설정
	//우선순위 설정
	@Bean
	public Trigger winnerJobTrigger(JobDetail winnerJobDetail) {
		return TriggerBuilder.newTrigger()
			.forJob(winnerJobDetail)
			.withIdentity("winnerJobTrigger")
			.withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?")) // 매일 2시
			.build();
	}
}
