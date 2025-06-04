package com.example.taste.domain.pk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.pk.entity.PkLog;
import com.example.taste.domain.user.entity.User;

public interface PkLogRepository extends JpaRepository<PkLog, Long> {
	List<PkLog> findAllByUser(User user);
}
