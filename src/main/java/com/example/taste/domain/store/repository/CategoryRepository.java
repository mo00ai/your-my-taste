package com.example.taste.domain.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.store.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
