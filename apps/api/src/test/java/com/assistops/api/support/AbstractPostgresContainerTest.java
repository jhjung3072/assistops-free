package com.assistops.api.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractPostgresContainerTest {

	private static final DockerImageName POSTGRES_IMAGE = DockerImageName
		.parse("pgvector/pgvector:pg16")
		.asCompatibleSubstituteFor("postgres");

	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
		.withDatabaseName("assistops")
		.withUsername("assistops")
		.withPassword("assistops");

	static {
		postgres.start();
	}

	@DynamicPropertySource
	static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}
}
