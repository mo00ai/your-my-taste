package com.example.taste.domain.party.validator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.taste.domain.party.repository.PartyInvitationRepository;

@Service
@RequiredArgsConstructor
public class PartyValidator {
	private final PartyInvitationRepository partyInvitationRepository;

	// 쿼리 DSL 리팩토링부터 하자
}
