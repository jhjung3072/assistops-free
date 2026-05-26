package com.assistops.api.global.health;

public record HealthResponse(String status, String service, String phase, String database) {

	private static final String OK_STATUS = "OK";
	private static final String SERVICE_NAME = "assistops-api";
	private static final String CURRENT_PHASE = "RAG Answer API & Q&A UI";

	public static HealthResponse ok(String database) {
		return new HealthResponse(OK_STATUS, SERVICE_NAME, CURRENT_PHASE, database);
	}
}
