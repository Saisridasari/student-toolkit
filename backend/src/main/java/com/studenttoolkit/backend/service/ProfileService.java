package com.studenttoolkit.backend.service;

import com.studenttoolkit.backend.dto.ProfileDto;
import com.studenttoolkit.backend.entity.User;
import com.studenttoolkit.backend.exception.ResourceNotFoundException;
import com.studenttoolkit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Profile service handling user profile operations.
 * Allows users to view and update their profile information.
 * Never exposes the password field in profile responses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    /**
     * Get the profile of a user by their email.
     * Returns a ProfileDto that excludes sensitive data like password.
     * 
     * @param email User's email (extracted from JWT token)
     * @return ProfileDto with user profile information
     */
    public ProfileDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return ProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .college(user.getCollege())
                .semester(user.getSemester())
                .role(user.getRole().name())
                .active(user.getActive())
                .build();
    }

    /**
     * Update the profile of a user.
     * Only allows updating non-sensitive fields (name, phone, department, etc).
     * Email and password changes require separate endpoints for security.
     * 
     * @param email User's email (extracted from JWT token)
     * @param profileDto Updated profile data
     * @return Updated ProfileDto
     */
    @Transactional
    public ProfileDto updateProfile(String email, ProfileDto profileDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Update only the allowed fields
        user.setFullName(profileDto.getFullName());
        user.setPhone(profileDto.getPhone());
        user.setDepartment(profileDto.getDepartment());
        user.setCollege(profileDto.getCollege());
        user.setSemester(profileDto.getSemester());

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", email);

        return ProfileDto.builder()
                .id(updatedUser.getId())
                .email(updatedUser.getEmail())
                .fullName(updatedUser.getFullName())
                .phone(updatedUser.getPhone())
                .department(updatedUser.getDepartment())
                .college(updatedUser.getCollege())
                .semester(updatedUser.getSemester())
                .role(updatedUser.getRole().name())
                .active(updatedUser.getActive())
                .build();
    }

    /**
     * Get profile of a user by their ID (used by admin dashboard).
     * 
     * @param userId User's ID
     * @return ProfileDto with user profile information
     */
    public ProfileDto getProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return ProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .college(user.getCollege())
                .semester(user.getSemester())
                .role(user.getRole().name())
                .active(user.getActive())
                .build();
    }
}