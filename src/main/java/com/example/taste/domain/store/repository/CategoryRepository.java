package com.example.taste.domain.store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.taste.domain.store.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	boolean existsByName(String name);

	Optional<Category> findByName(String name);

	@Query("SELECT MAX(c.displayOrder) FROM Category c")
	Optional<Integer> findMaxDisplayOrder();
}
