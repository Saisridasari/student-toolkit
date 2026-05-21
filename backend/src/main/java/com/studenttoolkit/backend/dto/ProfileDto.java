package com.studenttoolkit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user profile information.
 * Used to return user details without exposing sensitive data like password.
 * Also used for profile update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDto {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String department;
    private String college;
    private String semester;
    private String role;
    private Boolean active;
}