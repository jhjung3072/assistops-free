package com.assistops.api.global.health;

public record HealthResponse(String status, String service, String phase) {

	private static final String OK_STATUS = "OK";
	private static final String SERVICE_NAME = "assistops-api";
	private static final String CURRENT_PHASE = "Phase 1 - Spring Boot API Foundation";

	public static HealthResponse ok() {
		return new HealthResponse(OK_STATUS, SERVICE_NAME, CURRENT_PHASE);
	}
}
