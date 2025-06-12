package com.example.taste.domain.store.init;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.repository.CategoryRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component    // TODO: 추후 프로필 추가하여 개발 환경과 배포 환경에서 분리 필요 application-local.properties로?
@RequiredArgsConstructor
public class CategoryInitializer {
	private final CategoryRepository categoryRepository;

	@PostConstruct
	public void initCategorys() {
		List<String> categoryList = List.of("한식", "일식", "중식", "양식");

		for (int i = 1; i <= categoryList.size(); i++) {
			String name = categoryList.get(i - 1);
			int displayOrder = i;
			if (!categoryRepository.existsByName(name)) {
				Category category = Category.builder()
					.name(name)
					.displayOrder(displayOrder)
					.build();
				categoryRepository.save(category);
			}
		}
	}
}
