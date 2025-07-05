package com.example.taste.domain.notification.repository.webPush;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.notification.entity.WebPushSubscription;

@Repository
public interface WebPushRepository extends JpaRepository<WebPushSubscription, Long>, WebPushRepositoryCustom {
	Optional<WebPushSubscription> findByFcmToken(String fcmToken);
}
