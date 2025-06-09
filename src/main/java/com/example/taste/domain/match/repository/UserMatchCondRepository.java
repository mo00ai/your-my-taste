package com.example.taste.domain.match.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.match.entity.UserMatchCond;
import com.example.taste.domain.user.entity.User;

@Repository
public interface UserMatchCondRepository extends JpaRepository<UserMatchCond, Long> {
	List<UserMatchCond> findAllByUser(User user);
}
