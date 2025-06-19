package com.example.taste.common.batch;

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
public class BatchLauncherJob extends QuartzJobBean {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobRegistry jobRegistry;

	@Override
	protected void executeInternal(JobExecutionContext context) {
		try {
			String jobName = context.getJobDetail().getJobDataMap().getString("jobName");
			Job job = jobRegistry.getJob(jobName);

			JobParameters params = new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis()) // 유니크 파라미터
				.toJobParameters();

			jobLauncher.run(job, params);
		} catch (Exception e) {
			throw new RuntimeException("Quartz에서 Batch 실행 실패", e);
		}
	}
}

