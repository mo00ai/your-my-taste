package com.example.taste.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
