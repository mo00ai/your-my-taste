package com.example.taste.domain.favor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.favor.entity.Favor;

@Repository
public interface FavorRepository extends JpaRepository<Favor, Long> {
	Favor findByName(String favorName);

	boolean existsByName(String favorName);

	List<Favor> findAllByNameIn(List<String> favorNameList);
}
