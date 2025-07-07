package com.example.taste.domain.party.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.PartyFilter;

public interface PartyRepositoryCustom {
	Slice<Party> findAllByFilterAndSorted(Long userId, PartyFilter filter, Pageable pageable);

	List<Party> findAllMeetingDateAndDeleteAtIsNull(LocalDate meetingDate);
}
