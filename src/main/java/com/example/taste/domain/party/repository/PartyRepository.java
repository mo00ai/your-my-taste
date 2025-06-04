package com.example.taste.domain.party.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.party.entity.Party;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
}
