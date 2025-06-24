package com.example.taste.domain.notification.repository.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.notification.entity.UserNotificationDenySetting;
import com.example.taste.domain.user.entity.User;

@Repository
public interface UserNotificationDenySettingRepository extends JpaRepository<UserNotificationDenySetting, Long> {
	List<UserNotificationDenySetting> findAllByUser(User user);
}
