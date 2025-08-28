package com.example.digigoods.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user profile update request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {

  @Email(message = "Email should be valid")
  private String email;

  @Size(max = 50, message = "First name should not exceed 50 characters")
  private String firstName;

  @Size(max = 50, message = "Last name should not exceed 50 characters")
  private String lastName;

  @Size(max = 20, message = "Phone number should not exceed 20 characters")
  private String phoneNumber;
}
