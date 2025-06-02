package com.example.taste.domain.favor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.favor.entity.Favor;

@Repository
public interface FavorRepository extends JpaRepository<Favor, Long> {
}
