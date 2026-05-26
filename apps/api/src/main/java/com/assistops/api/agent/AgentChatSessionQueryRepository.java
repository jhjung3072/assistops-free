package com.assistops.api.agent;

import static com.assistops.api.agent.QAgentChatSession.agentChatSession;

import com.assistops.api.global.response.PageRequestFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class AgentChatSessionQueryRepository {

	private final JPAQueryFactory queryFactory;

	public AgentChatSessionQueryRepository(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	public Page<AgentChatSession> search(UUID userId, AgentChatSessionSearchCondition condition) {
		PageRequest pageable = PageRequestFactory.create(
			condition.page(),
			condition.size(),
			Sort.by(Sort.Direction.DESC, "updatedAt")
		);

		List<AgentChatSession> content = queryFactory
			.selectFrom(agentChatSession)
			.where(
				agentChatSession.userId.eq(userId),
				keywordContains(condition.keyword()),
				createdAtGoe(condition.createdFrom()),
				createdAtLoe(condition.createdTo())
			)
			.orderBy(agentChatSession.updatedAt.desc(), agentChatSession.id.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(agentChatSession.count())
			.from(agentChatSession)
			.where(
				agentChatSession.userId.eq(userId),
				keywordContains(condition.keyword()),
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

		return agentChatSession.title.containsIgnoreCase(keyword.trim());
	}

	private BooleanExpression createdAtGoe(LocalDateTime createdFrom) {
		return createdFrom == null ? null : agentChatSession.createdAt.goe(createdFrom.toInstant(ZoneOffset.UTC));
	}

	private BooleanExpression createdAtLoe(LocalDateTime createdTo) {
		return createdTo == null ? null : agentChatSession.createdAt.loe(createdTo.toInstant(ZoneOffset.UTC));
	}
}
