package com.studenttoolkit.backend.repository;

import com.studenttoolkit.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity providing CRUD operations and custom queries.
 * Spring Data JPA automatically implements common database operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email - used during login and registration
     * to check if a user already exists with the given email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given email - used during
     * registration to quickly verify email uniqueness.
     */
    Boolean existsByEmail(String email);

    /**
     * Count total number of users - used in admin dashboard statistics.
     */
    long count();
}