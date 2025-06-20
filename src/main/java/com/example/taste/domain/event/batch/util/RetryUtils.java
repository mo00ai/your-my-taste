package com.example.taste.domain.event.batch.util;

import java.util.concurrent.Callable;

import org.springframework.dao.DataAccessException;

import com.example.taste.common.exception.CustomException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryUtils {

	public static <T> T executeWithRetry(Callable<T> task, int maxRetries, String context) {
		int retryCount = 0;
		while (retryCount < maxRetries) {

			try {

				return task.call();

			} catch (Exception e) {
				retryCount++;

				// 예외 유형별 로그 분기
				if (e instanceof DataAccessException) {
					logWarn(context, "DB 오류", e, retryCount);
				} else if (e instanceof CustomException) {
					logWarn(context, "비즈니스 예외", e, retryCount);
				} else {
					logWarn(context, "기타 예외", e, retryCount);
				}

				// 재시도 중단 조건
				if (retryCount >= maxRetries) {
					log.error("[최종 실패] {} - {}회 시도, 예외: {}", context, retryCount, e.toString());
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}

	private static void logWarn(String context, String type, Exception e, int retryCount) {
		log.warn("[재시도 {}/{}] {} - {}: {}", retryCount, 3, context, type, e.getMessage());
	}
}
