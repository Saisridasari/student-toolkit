package com.studenttoolkit.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter that intercepts every HTTP request to validate the JWT token.
 * 
 * This filter runs once per request and performs the following:
 * 1. Extracts the JWT token from the Authorization header (format: "Bearer <token>")
 * 2. Validates the token using JwtUtil
 * 3. Loads the user details from the database using UserDetailsService
 * 4. Sets the authentication in the Spring Security context
 * 
 * Once the authentication is set in the SecurityContext, the request proceeds
 * to the next filter or the controller with the authenticated user information.
 * 
 * If no valid token is found, the request proceeds without authentication
 * and will be handled by Spring Security's authorization rules (which may deny access).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Extract Authorization header from the request
        final String authHeader = request.getHeader("Authorization");

        // Check if the header starts with "Bearer " (standard JWT header format)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // No JWT token found, proceed without authentication
        }

        // Extract the JWT token by removing the "Bearer " prefix
        final String jwtToken = authHeader.substring(7);

        // Extract username (email) from the token
        final String username = jwtUtil.extractUsername(jwtToken);

        // If username is found and no authentication is already set in the context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user details from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Validate the token against the loaded user details
            if (jwtUtil.validateToken(jwtToken, userDetails)) {

                // Create an authentication token with the user details and authorities
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Set additional details (like remote address, session ID)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in the SecurityContext
                // This tells Spring Security that the user is authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("User {} authenticated successfully", username);
            } else {
                log.warn("JWT token validation failed for user: {}", username);
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}