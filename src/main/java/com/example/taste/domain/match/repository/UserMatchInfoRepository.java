package com.example.taste.domain.match.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.user.entity.User;

@Repository
public interface UserMatchInfoRepository extends JpaRepository<UserMatchInfo, Long> {
	List<UserMatchInfo> findAllByUser(User user);

	MatchStatus findUserMatchStatusById(Long id);

	List<UserMatchInfo> findAllByMatchStatus(MatchStatus matchStatus);
}
