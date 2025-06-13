package com.example.taste.domain.favor.init;

import java.util.List;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.favor.repository.FavorRepository;

@Component    // TODO: 추후 프로필 추가하여 개발 환경과 배포 환경에서 분리 필요 - @윤예진
@RequiredArgsConstructor
public class FavorInitializer {
	private final FavorRepository favorRepository;

	@PostConstruct
	public void initFavors() {
		List<String> favorList = List.of(
			"단짠단짠", "매콤달콤", "부먹", "찍먹", "소식파", "대식파"
		);
		for (String name : favorList) {
			// 중복 저장이 아니라면
			if (!favorRepository.existsByName(name)) {
				favorRepository.save(new Favor(name));
			}
		}
	}
}
