package com.studenttoolkit.backend.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS configuration is now handled directly in SecurityConfig.java
 * as part of the Spring Security filter chain configuration.
 * 
 * This class is kept as a reference but is no longer active since
 * SecurityConfig handles CORS configuration more comprehensively
 * by integrating it with the security filter chain.
 * 
 * The CORS settings in SecurityConfig allow:
 * - All origins (should be restricted in production)
 * - Standard HTTP methods (GET, POST, PUT, DELETE, OPTIONS, PATCH)
 * - All headers including Authorization
 * - Exposed Authorization header for JWT token access
 * - 1-hour CORS preflight cache
 */
@Configuration
public class CorsConfig {
    // CORS configuration is now handled in SecurityConfig.java
    // This class is intentionally left empty as a reference marker.
    // The actual CORS configuration is in SecurityConfig.securityFilterChain()
}
