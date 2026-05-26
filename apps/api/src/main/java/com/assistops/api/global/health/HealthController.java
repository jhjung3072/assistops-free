package com.assistops.api.global.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health")
@RestController
@RequestMapping("/api/health")
public class HealthController {

	@Operation(summary = "Get API health status")
	@GetMapping
	public HealthResponse getHealth() {
		return HealthResponse.ok();
	}
}
