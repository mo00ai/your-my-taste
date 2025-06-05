package com.example.taste.domain.pk.init;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.taste.domain.pk.entity.PkCriteria;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.repository.PkCriteriaRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PkCriteriaInitializer {

	private final PkCriteriaRepository pkCriteriaRepository;

	@PostConstruct
	public void initFavors() {
		Map<PkType, Integer> pkTypePointMap = Map.of(PkType.POST, 30, PkType.REVIEW, 20, PkType.LIKE, 20, PkType.EVENT,
			500);

		for (Map.Entry<PkType, Integer> entry : pkTypePointMap.entrySet()) {
			if (!pkCriteriaRepository.existsByType(entry.getKey())) {
				PkCriteria pkCriteria = PkCriteria.builder()
					.type(entry.getKey())
					.point(entry.getValue())
					.active(true)
					.build();
				pkCriteriaRepository.save(pkCriteria);
			}
		}
	}

}
