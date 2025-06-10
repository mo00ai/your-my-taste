package com.example.taste.domain.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.match.entity.PartyMatchCond;

public interface PartyMatchCondRepository extends JpaRepository<PartyMatchCond, Long> {
}
