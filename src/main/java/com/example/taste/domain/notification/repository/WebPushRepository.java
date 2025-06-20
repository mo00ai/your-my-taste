package com.example.taste.domain.notification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.notification.entity.WebPushInformation;

@Repository
public interface WebPushRepository extends JpaRepository<WebPushInformation, Long> {
	Optional<WebPushInformation> findByEndpoint(String endpoint);
}
