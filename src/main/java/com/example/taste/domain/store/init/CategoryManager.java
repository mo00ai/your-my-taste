package com.example.taste.domain.store.init;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.repository.CategoryRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryManager {
	private final CategoryRepository categoryRepository;
	private Set<String> foodCategories = new HashSet<>();

	@PostConstruct
	public void loadCategories() {
		this.foodCategories = categoryRepository.findAll()
			.stream()
			.map(Category::getName)
			.collect(Collectors.toSet());
	}

	public boolean isFoodCategory(String word) {
		return foodCategories.contains(word);
	}

	public Set<String> getFoodCategories() {
		return foodCategories;
	}
}
