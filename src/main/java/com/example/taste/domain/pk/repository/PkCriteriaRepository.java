package com.example.taste.domain.pk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.pk.entity.PkCriteria;

public interface PkCriteriaRepository extends JpaRepository<PkCriteria, Long> {
}
