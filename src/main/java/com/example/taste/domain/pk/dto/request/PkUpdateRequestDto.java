package com.example.taste.domain.pk.dto.request;

import org.hibernate.validator.constraints.Length;

import com.example.taste.common.annotation.ValidEnum;
import com.example.taste.domain.pk.enums.PkType;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PkUpdateRequestDto {

	@ValidEnum(message = "등록되지 않은 Pk 포인트 유형 값입니다.", target = PkType.class)
	@Length(max = 20, message = "Pk 포인트 유형은 20자 이내로 작성해주세요. ")
	private String type;

	@Min(value = 0, message = "포인트는 0 이상이어야 합니다.")
	private Integer point;

}
