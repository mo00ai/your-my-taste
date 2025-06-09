package com.example.taste.domain.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.match.entity.UserMatchCondStore;

public interface UserMatchCondStoreRepository extends JpaRepository<UserMatchCondStore, Long> {
}
