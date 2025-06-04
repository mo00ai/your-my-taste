package com.example.taste.domain.party.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.party.entity.Party;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
	@Query("SELECT DISTINCT p FROM Party p LEFT JOIN FETCH p.hostUser LEFT JOIN FETCH p.store "
		+ "WHERE p.hostUser.id != :hostUserId")
	List<Party> findAllByUserNot(@Param("hostUserId") Long hostUserId);

	@Query(
		"SELECT DISTINCT pi.party FROM PartyInvitation pi "
			+ "LEFT JOIN FETCH pi.party.hostUser LEFT JOIN FETCH pi.party.store "
			+ "WHERE pi.user.id = :userId")
	List<Party> findAllByUserIn(@Param("userId") Long userId);
}
