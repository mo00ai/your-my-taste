package com.example.taste.domain.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.match.entity.UserMatchInfoCategory;

public interface UserMatchInfoCategoryRepository extends JpaRepository<UserMatchInfoCategory, Long> {
}
