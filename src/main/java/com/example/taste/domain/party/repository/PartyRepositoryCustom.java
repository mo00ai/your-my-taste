package com.example.taste.domain.party.repository;

import java.util.List;

import com.example.taste.domain.party.entity.Party;

public interface PartyRepositoryCustom {
	List<Party> findAllByRecruitingAndUserNotIn(Long userId);

	List<Party> findAllByUserIn(Long userId);

}
