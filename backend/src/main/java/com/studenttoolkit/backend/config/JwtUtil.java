package com.studenttoolkit.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Utility class responsible for generating, validating, and parsing JWT tokens.
 * 
 * JWT (JSON Web Token) is used for stateless authentication:
 * 1. User logs in -> server generates a JWT token containing user info
 * 2. Client stores the token and sends it in the Authorization header of each request
 * 3. Server validates the token on each request to identify the user
 * 
 * The token contains:
 * - Subject: user email (unique identifier)
 * - Claims: user role and other metadata
 * - IssuedAt: token creation timestamp
 * - Expiration: token expiry timestamp (24 hours by default)
 * 
 * The token is signed with a secret key to prevent tampering.
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Generate a signing key from the secret string.
     * The key must be at least 256 bits (32 bytes) for HS256 algorithm.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a JWT token for the given user.
     * The token contains the user's email as subject and role as a claim.
     * 
     * @param userDetails Spring Security UserDetails object containing user info
     * @return JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Add role claim for role-based authorization
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Create a JWT token with the given claims and subject.
     * Sets issued-at and expiration timestamps.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject) // Subject is the user's email
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey()) // Sign with HMAC-SHA256
                .compact();
    }

    /**
     * Extract the username (email) from the JWT token.
     * This is used to identify the user during request authentication.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the expiration date from the JWT token.
     * Used to check if the token has expired.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from the token using a resolver function.
     * Generic method that can extract any claim type from the token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from the JWT token.
     * Parses the token and verifies the signature to ensure it hasn't been tampered with.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if the JWT token has expired.
     * Compares the token's expiration date with the current time.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate the JWT token against the given user details.
     * Checks that:
     * 1. The token's subject matches the user's email
     * 2. The token has not expired
     * 
     * @param token JWT token string
     * @param userDetails Spring Security UserDetails object
     * @return true if the token is valid, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Extract the user role from the JWT token.
     * Used for role-based access control in security configuration.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
}