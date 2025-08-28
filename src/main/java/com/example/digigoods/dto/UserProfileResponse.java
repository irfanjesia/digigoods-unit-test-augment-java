package com.example.digigoods.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user profile response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

  private Long id;
  private String username;
  private String email;
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
