package com.example.taste.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findById(Long id);
}
