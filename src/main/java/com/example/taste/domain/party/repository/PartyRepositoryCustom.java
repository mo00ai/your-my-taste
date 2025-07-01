package com.example.taste.domain.party.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.example.taste.domain.party.entity.Party;

public interface PartyRepositoryCustom {
	Slice<Party> findAllByActiveAndUserNotInSorted(Long userId, Pageable pageable);

	Slice<Party> findAllByUserInSorted(Long userId, Pageable pageable);

	List<Party> findAllMeetingDateAndDeleteAtIsNull(LocalDate meetingDate);
}
