package com.example.taste.domain.pk.scheduler;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.taste.domain.pk.service.PkService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PkTermScheduler {

	private final PkService pkService;

	@Scheduled(cron = "0 0 0 1 * *")
	public void changePkTerm() {
		pkService.runPkTermRankingScheduler(LocalDate.now());
	}

}
