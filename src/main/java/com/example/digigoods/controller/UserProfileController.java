package com.example.digigoods.controller;

import com.example.digigoods.dto.UserProfileRequest;
import com.example.digigoods.dto.UserProfileResponse;
import com.example.digigoods.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user profile endpoints.
 */
@RestController
@RequestMapping("/users")
public class UserProfileController {

  private final UserProfileService userProfileService;

  public UserProfileController(UserProfileService userProfileService) {
    this.userProfileService = userProfileService;
  }

  /**
   * Get user profile by ID endpoint.
   *
   * @param userId the user ID
   * @return user profile response
   */
  @GetMapping("/{userId}/profile")
  public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
    UserProfileResponse profile = userProfileService.getUserProfile(userId);
    return ResponseEntity.ok(profile);
  }

  /**
   * Update user profile endpoint.
   *
   * @param userId the user ID
   * @param profileRequest the profile update request
   * @return updated user profile response
   */
  @PutMapping("/{userId}/profile")
  public ResponseEntity<UserProfileResponse> updateUserProfile(
      @PathVariable Long userId,
      @Valid @RequestBody UserProfileRequest profileRequest) {
    UserProfileResponse updatedProfile = userProfileService.updateUserProfile(userId,
        profileRequest);
    return ResponseEntity.ok(updatedProfile);
  }
}
