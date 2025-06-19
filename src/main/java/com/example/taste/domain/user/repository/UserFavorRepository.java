package com.example.taste.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.user.entity.UserFavor;

@Repository
public interface UserFavorRepository extends JpaRepository<UserFavor, Long> {
}
