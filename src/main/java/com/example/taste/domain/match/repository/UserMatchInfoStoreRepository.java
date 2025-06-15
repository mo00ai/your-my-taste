package com.example.taste.domain.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.match.entity.UserMatchInfoStore;

public interface UserMatchInfoStoreRepository extends JpaRepository<UserMatchInfoStore, Long> {
}
