package com.example.taste.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.chat.entity.Chat;
import com.example.taste.domain.party.entity.Party;

public interface ChatRepository extends JpaRepository<Chat, Long> {
	List<Chat> findAllByParty(Party party);
}
