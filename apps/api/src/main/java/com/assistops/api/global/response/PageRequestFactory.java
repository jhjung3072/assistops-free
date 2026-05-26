package com.assistops.api.global.response;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageRequestFactory {

	public static final int DEFAULT_PAGE = 0;
	public static final int DEFAULT_SIZE = 20;
	public static final int MAX_SIZE = 100;

	private PageRequestFactory() {
	}

	public static PageRequest create(Integer page, Integer size, Sort sort) {
		int resolvedPage = page == null ? DEFAULT_PAGE : Math.max(page, DEFAULT_PAGE);
		int resolvedSize = size == null ? DEFAULT_SIZE : Math.max(1, Math.min(size, MAX_SIZE));

		return PageRequest.of(resolvedPage, resolvedSize, sort);
	}
}
