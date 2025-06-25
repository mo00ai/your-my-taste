package com.example.taste.domain.notification.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.notification.entity.NotificationInfo;

@Repository
public interface NotificationInfoRepository
	extends JpaRepository<NotificationInfo, Long>, NotificationInfoRepositoryCustom {
}
