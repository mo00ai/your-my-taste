package com.example.taste.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.notification.entity.NotificationContent;

@Repository
public interface NotificationContentRepository extends JpaRepository<NotificationContent, Long> {
}
