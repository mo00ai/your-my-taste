package com.example.taste.domain.pk.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.taste.domain.pk.dto.response.PkCriteriaResponseDto;
import com.example.taste.domain.pk.entity.PkTerm;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.repository.PkTermRepository;

@ExtendWith(MockitoExtension.class)
class PkServiceUnitTest {

	@InjectMocks
	private PkService pkService;

	@Mock
	private PkCacheService pkCacheService;

	@Mock
	private PkTermRepository pkTermRepository;

	@Test
	void findAllPkCriteria_success() {
		// given
		List<PkCriteriaResponseDto> mockList = List.of(
			new PkCriteriaResponseDto(1L, "KIMCHI", 10, true)
		);
		given(pkCacheService.findAllPkCriteria()).willReturn(mockList);

		// when
		List<PkCriteriaResponseDto> result = pkService.findAllPkCriteria();

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getType()).isEqualTo("KIMCHI");
	}

	@Test
	void getPointByPkType_success() {
		// given
		List<PkCriteriaResponseDto> mockList = List.of(
			new PkCriteriaResponseDto(1L, "KIMCHI", 10, true)
		);
		given(pkCacheService.findAllPkCriteria()).willReturn(mockList);

		// when
		int point = pkService.getPointByPkType(PkType.POST);

		// then
		assertThat(point).isEqualTo(10);
	}

	@Test
	void getMaxPkTerm_success() {
		// given
		PkTerm latestTerm = PkTerm.builder().term(5).build();
		given(pkTermRepository.findTopByOrderByTermDesc()).willReturn(Optional.of(latestTerm));

		// when
		int nextTerm = ReflectionTestUtils.invokeMethod(pkService, "getMaxPkTerm");

		// then
		assertThat(nextTerm).isEqualTo(6);
	}
}
