package com.example.taste.domain.notification.repository.notification;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.notification.entity.UserNotificationSetting;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.user.entity.User;

@Repository
public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {
	List<UserNotificationSetting> findAllByUser(User user);

	Optional<UserNotificationSetting> findByUserAndNotificationCategory(User user, NotificationCategory category);

}
