package com.example.taste.domain.pk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.pk.entity.PkTermRanking;

public interface PkTermRankingRepository extends JpaRepository<PkTermRanking, Long> {
	
}
