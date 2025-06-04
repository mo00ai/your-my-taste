package com.example.taste.domain.pk.dto.request;

import org.hibernate.validator.constraints.Length;

import com.example.taste.domain.pk.annotation.ValidPkType;
import com.example.taste.domain.pk.enums.PkType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PkSaveRequestDto {

	@ValidPkType(message = "등록되지 않은 Pk 포인트 유형 값입니다.", target = PkType.class)
	@NotBlank(message = "Pk 포인트 유형은 필수값입니다.")
	@Length(max = 20, message = "Pk 포인트 유형은 20자 이내로 작성해주세요. ")
	private String type;

	@NotNull(message = "Pk 포인트는 필수값입니다.")
	@Min(value = 0, message = "포인트는 0 이상이어야 합니다.")
	private Integer point;

}
