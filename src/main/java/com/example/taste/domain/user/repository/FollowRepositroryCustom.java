package com.example.taste.domain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FollowRepositroryCustom {
	Page<Long> findAllIdByFollowing(Long followingId, PageRequest pageRequest);
}
