package com.example.taste.domain.pk.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.pk.entity.PkCriteria;
import com.example.taste.domain.pk.enums.PkType;

public interface PkCriteriaRepository extends JpaRepository<PkCriteria, Long> {
	boolean existsByType(PkType type);

	Optional<PkCriteria> findByType(PkType type);
}
