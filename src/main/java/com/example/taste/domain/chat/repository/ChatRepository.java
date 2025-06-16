package com.example.taste.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.chat.entity.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {
	List<Chat> findAllByPartyIdOrderByCreatedAtAsc(Long partyId);
}
