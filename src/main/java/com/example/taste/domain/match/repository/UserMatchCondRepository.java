package com.example.taste.domain.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.match.entity.UserMatchCond;

@Repository
public interface UserMatchCondRepository extends JpaRepository<UserMatchCond, Long> {
}
