package com.assistops.api.rag;

import static com.assistops.api.rag.QRagAnswer.ragAnswer;

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
public class RagAnswerQueryRepository {

	private final JPAQueryFactory queryFactory;

	public RagAnswerQueryRepository(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	public Page<RagAnswer> search(UUID userId, Collection<UUID> workspaceIds, RagAnswerSearchCondition condition) {
		PageRequest pageable = PageRequestFactory.create(
			condition.page(),
			condition.size(),
			Sort.by(Sort.Direction.DESC, "createdAt")
		);

		if (workspaceIds.isEmpty()) {
			return new PageImpl<>(List.of(), pageable, 0);
		}

		List<RagAnswer> content = queryFactory
			.selectFrom(ragAnswer)
			.where(
				ragAnswer.userId.eq(userId),
				ragAnswer.workspaceId.in(workspaceIds),
				keywordContains(condition.keyword()),
				modelContains(condition.model()),
				createdAtGoe(condition.createdFrom()),
				createdAtLoe(condition.createdTo())
			)
			.orderBy(ragAnswer.createdAt.desc(), ragAnswer.id.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(ragAnswer.count())
			.from(ragAnswer)
			.where(
				ragAnswer.userId.eq(userId),
				ragAnswer.workspaceId.in(workspaceIds),
				keywordContains(condition.keyword()),
				modelContains(condition.model()),
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

		String trimmedKeyword = keyword.trim();
		return ragAnswer.question.containsIgnoreCase(trimmedKeyword)
			.or(ragAnswer.answer.containsIgnoreCase(trimmedKeyword));
	}

	private BooleanExpression modelContains(String model) {
		if (!StringUtils.hasText(model)) {
			return null;
		}

		return ragAnswer.model.containsIgnoreCase(model.trim());
	}

	private BooleanExpression createdAtGoe(LocalDateTime createdFrom) {
		return createdFrom == null ? null : ragAnswer.createdAt.goe(createdFrom.toInstant(ZoneOffset.UTC));
	}

	private BooleanExpression createdAtLoe(LocalDateTime createdTo) {
		return createdTo == null ? null : ragAnswer.createdAt.loe(createdTo.toInstant(ZoneOffset.UTC));
	}
}
