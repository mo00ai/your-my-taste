package com.example.taste.domain.pk.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.pk.entity.PkTerm;

public interface PkTermRepository extends JpaRepository<PkTerm, Long> {

	Optional<PkTerm> findByTerm(int maxTerm);

	Optional<PkTerm> findTopByOrderByTermDesc();
}
