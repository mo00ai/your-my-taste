package com.example.taste.fixtures;

import com.example.taste.domain.pk.entity.PkCriteria;
import com.example.taste.domain.pk.enums.PkType;

public class PkCriteriaFixture {

	public static PkCriteria create() {
		return PkCriteria.builder()
			.active(true)
			.type(PkType.POST)
			.point(50)
			.build();
	}
}
