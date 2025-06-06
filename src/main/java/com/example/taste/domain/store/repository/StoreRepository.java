package com.example.taste.domain.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.store.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
