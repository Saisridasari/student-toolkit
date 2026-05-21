package com.studenttoolkit.backend.config;

import com.studenttoolkit.backend.entity.User;
import com.studenttoolkit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration class that sets up the entire security framework.
 * 
 * Key configurations:
 * 1. JWT-based stateless authentication (no sessions)
 * 2. BCrypt password encoding for secure password storage
 * 3. CORS integration for frontend communication
 * 4. Public endpoints (login, register, contact) vs protected endpoints
 * 5. Role-based access control (STUDENT vs ADMIN)
 * 
 * Security flow:
 * - Request arrives -> JwtAuthenticationFilter validates token
 * - If valid, user is authenticated and request proceeds
 * - If invalid or missing, Spring Security checks endpoint permissions
 * - Public endpoints allow access without authentication
 * - Protected endpoints require valid JWT token
 * - Admin endpoints require ADMIN role in the token
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize annotations for method-level security
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserRepository userRepository;

    /**
     * Configure CORS separately as a bean so it can be used by both
     * Spring Security and the web MVC CORS filter.
     * 
     * In production, replace "*" origins with specific frontend URLs
     * like "http://localhost:3000" for React development server.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // In production, specify exact origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setMaxAge(3600L); // Cache CORS preflight response for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configure the security filter chain that processes every HTTP request.
     * 
     * - Disable CSRF (not needed for JWT-based stateless authentication)
     * - Configure CORS to allow frontend connections
     * - Set session management to STATELESS (JWT handles authentication, not sessions)
     * - Define which endpoints are public and which require authentication
     * - Add JWT filter before the standard username/password filter
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF - JWT tokens are immune to CSRF attacks
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS using the corsConfigurationSource bean
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure endpoint authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers(
                                "/api/auth/**",          // Login and registration
                                "/api/test/**",          // Test endpoints
                                "/api/contact/**",       // Contact form (anonymous users can submit)
                                "/api/public/**",        // Public resources
                                "/api/notes/public",     // Public notes listing (browse without auth)
                                "/v3/api-docs/**",       // Swagger/OpenAPI documentation
                                "/swagger-ui/**",        // Swagger UI
                                "/swagger-ui.html"       // Swagger UI entry point
                        ).permitAll()

                        // Admin endpoints - require ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // All other endpoints - require authentication
                        .anyRequest().authenticated()
                )

                // Set session management to stateless - no HTTP sessions will be created
                // This is essential for JWT-based authentication
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Set the authentication provider (DaoAuthenticationProvider with BCrypt)
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before the standard Spring Security filter
                // This ensures JWT token validation happens before username/password processing
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure the authentication provider that loads user details and verifies passwords.
     * Uses DaoAuthenticationProvider which:
     * 1. Loads user by email using UserDetailsService
     * 2. Compares the provided password with the stored BCrypt-encoded password
     * 3. Returns authenticated UserDetails if credentials match
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Provide UserDetailsService implementation that loads users from the database.
     * Spring Security uses this to load user details during authentication.
     * Converts our User entity to Spring Security's UserDetails format.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User not found with email: " + username));

            // Convert our User entity to Spring Security UserDetails
            // The role is prefixed with "ROLE_" as required by Spring Security's hasRole() checks
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name()) // ROLE_STUDENT or ROLE_ADMIN
                    .accountLocked(!user.getActive())
                    .build();
        };
    }

    /**
     * Provide BCrypt password encoder for secure password hashing.
     * BCrypt is a strong hashing algorithm that:
     * - Uses a salt to prevent rainbow table attacks
     * - Has an adjustable work factor to resist brute-force attacks
     * - Is the recommended password encoder by Spring Security
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provide AuthenticationManager for authenticating users during login.
     * Delegates to the configured AuthenticationProvider.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}