package com.assistops.api.global.health;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseHealthService {

	private static final String UP = "UP";
	private static final String DOWN = "DOWN";

	private final JdbcTemplate jdbcTemplate;

	public DatabaseHealthService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public String getDatabaseStatus() {
		try {
			jdbcTemplate.queryForObject("SELECT 1", Integer.class);
			return UP;
		}
		catch (DataAccessException exception) {
			return DOWN;
		}
	}
}
