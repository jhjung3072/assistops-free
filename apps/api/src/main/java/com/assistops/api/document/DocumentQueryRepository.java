package com.assistops.api.document;

import static com.assistops.api.document.QDocument.document;

import com.assistops.api.global.response.PageRequestFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class DocumentQueryRepository {

	private final JPAQueryFactory queryFactory;

	public DocumentQueryRepository(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	public Page<Document> search(Collection<UUID> workspaceIds, DocumentSearchCondition condition) {
		PageRequest pageable = PageRequestFactory.create(
			condition.page(),
			condition.size(),
			Sort.by(Sort.Direction.DESC, "createdAt")
		);

		if (workspaceIds.isEmpty()) {
			return new PageImpl<>(List.of(), pageable, 0);
		}

		List<Document> content = queryFactory
			.selectFrom(document)
			.where(
				document.workspaceId.in(workspaceIds),
				keywordContains(condition.keyword()),
				statusEqOrExcludeDeleted(condition.status()),
				embeddingStatusEq(condition.embeddingStatus()),
				createdAtGoe(condition.createdFrom()),
				createdAtLoe(condition.createdTo())
			)
			.orderBy(document.createdAt.desc(), document.id.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(document.count())
			.from(document)
			.where(
				document.workspaceId.in(workspaceIds),
				keywordContains(condition.keyword()),
				statusEqOrExcludeDeleted(condition.status()),
				embeddingStatusEq(condition.embeddingStatus()),
				createdAtGoe(condition.createdFrom()),
				createdAtLoe(condition.createdTo())
			)
			.fetchOne();

		return new PageImpl<>(content, pageable, total == null ? 0 : total);
	}

	private BooleanExpression keywordContains(String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return null;
		}

		return document.originalFilename.containsIgnoreCase(keyword.trim());
	}

	private BooleanExpression statusEqOrExcludeDeleted(DocumentStatus status) {
		if (status == null) {
			return document.status.ne(DocumentStatus.DELETED);
		}

		return document.status.eq(status);
	}

	private BooleanExpression embeddingStatusEq(DocumentEmbeddingStatus embeddingStatus) {
		return embeddingStatus == null ? null : document.embeddingStatus.eq(embeddingStatus);
	}

	private BooleanExpression createdAtGoe(LocalDateTime createdFrom) {
		return createdFrom == null ? null : document.createdAt.goe(createdFrom.toInstant(ZoneOffset.UTC));
	}

	private BooleanExpression createdAtLoe(LocalDateTime createdTo) {
		return createdTo == null ? null : document.createdAt.loe(createdTo.toInstant(ZoneOffset.UTC));
	}
}
