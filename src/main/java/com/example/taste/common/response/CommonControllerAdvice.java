package com.example.taste.common.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
//컨트롤러 끝나면 모두다 돌려주는게 아님 -> 메서드들이 매핑만 확인, api 리퀘스트 핸들러에 넣어주는 역할만함
//어드바이스로 적용범위가 달라짐 -> 역할을 다르게 해줌 -> 어노테이션
public class CommonControllerAdvice implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		// 프로메테우스는 CommonResponse 말고 일반 응답
		if (returnType.getContainingClass().getName().contains("PrometheusEndpoint")) {
			return false;
		}
		return !Void.TYPE.equals(returnType.getParameterType());
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
		Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
		ServerHttpResponse response) {

		// prometheus 엔드포인트는 감싸지 말기
		String path = request.getURI().getPath();
		if (path.startsWith("/actuator/prometheus")) {
			return body;
		}

		//CommonResponse 반환, 상태코드와 함께
		//created, 에러 등
		if (body instanceof CommonResponse<?> commonBody) {
			response.setStatusCode(commonBody.getStatus());
			return commonBody;
		}

		//response객체 없을때
		if (body == null) {
			return CommonResponse.ok();
		}

		//response객체 있는 ok상태코드 반환
		return CommonResponse.ok(body);

	}

}
