package com.example.taste.config.quartz.job;

import org.quartz.JobExecutionContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class QuartzBatchLauncherJob extends QuartzJobBean {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobRegistry jobRegistry;

	@Override
	protected void executeInternal(JobExecutionContext context) {
		try {
			Job job = jobRegistry.getJob("testJob1");
			JobParameters params = new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis()) // 유니크 파라미터
				.toJobParameters();
			jobLauncher.run(job, params);
		} catch (Exception e) {
			throw new RuntimeException("Quartz에서 Batch 실행 실패", e);
		}
	}
}