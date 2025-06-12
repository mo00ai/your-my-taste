package com.example.taste.domain.store.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.user.entity.User;

public interface StoreBucketRepositoryCustom {
	// 내 맛집 리스트(버킷) 중 키워드가 포함된 버킷조회
	Page<StoreBucket> searchMyBuckets(User user, String keyword, Pageable pageable);

	// 내 팔로우 맛집 리스트(버킷) 중 키워드가 포함된 버킷조회
	Page<StoreBucket> searchFollowingsBucketsWithKeyword(List<Long> followingIds, String keyword, Pageable pageable);

}
