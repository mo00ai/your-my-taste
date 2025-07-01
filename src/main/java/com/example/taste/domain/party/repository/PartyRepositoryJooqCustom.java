package com.example.taste.domain.party.repository;

import java.time.LocalDate;
import java.util.List;

public interface PartyRepositoryJooqCustom {
	List<Long> findAllByMeetingDate(LocalDate meetingDate);

	long updateExpiredStateByIds(List<? extends Long> ids);

	long softDeleteByIds(List<? extends Long> ids);
}