package com.example.taste.common.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class PageResponse<T> {

	private final List<T> content; //게시글 목록

	private final int nowPage; //현재 페이지

	private final int pageSize; // 한 페이지 당 몇 개의 게시글인지

	private final int totalPages; //전체 페이지

	private final boolean hasNext; // 화살표

	private final boolean hasPrevious; //이전 화살표

	private final int startPage; // 페지징 범위 (현재 페이지 기준 -2)

	private final int endPage; //페이징 범위 (현재 페이지 기준 +2)

	public static <T> PageResponse<T> from(Page<T> allPage) {

		int nowPage = allPage.getNumber() + 1;
		int pageRange = 5;
		int totalPages = allPage.getTotalPages();
		int startPage = ((nowPage - 1) / pageRange) * pageRange + 1;
		int endPage = Math.min(startPage + pageRange - 1, totalPages);
		boolean hasPrevious = startPage > 1;
		boolean hasNext = endPage < totalPages;

		return PageResponse.<T>builder()
			.content(allPage.getContent())
			.nowPage(nowPage)
			.pageSize(allPage.getSize())
			.totalPages(totalPages)
			.hasNext(hasNext)
			.hasPrevious(hasPrevious)
			.startPage(startPage)
			.endPage(endPage)
			.build();
	}

	public static <T> PageResponse<T> fromRedis(List<T> allContent, int page, int size) {
		if (page < 1) {
			page = 1;
		}

		int totalElements = allContent.size();
		int totalPages = (allContent.size() + size - 1) / size;

		int fromIndex = (page - 1) * size;
		int toIndex = Math.min(fromIndex + size, totalElements);
		List<T> content = fromIndex >= totalElements ? List.of() : allContent.subList(fromIndex, toIndex);

		int pageRange = 5;
		int startPage = ((page - 1) / pageRange) * pageRange + 1;
		int endPage = Math.min(startPage + pageRange - 1, totalPages);

		return PageResponse.<T>builder()
			.content(content)
			.nowPage(page)
			.pageSize(size)
			.totalPages(totalPages)
			.hasPrevious(startPage > 1)
			.hasNext(endPage < totalPages)
			.startPage(startPage)
			.endPage(endPage)
			.build();
	}
}
