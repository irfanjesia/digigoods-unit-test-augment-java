package com.example.digigoods.service;

import com.example.digigoods.dto.UserProfileRequest;
import com.example.digigoods.dto.UserProfileResponse;
import com.example.digigoods.exception.UserNotFoundException;
import com.example.digigoods.model.User;
import com.example.digigoods.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user profile operations.
 */
@Service
@Transactional
public class UserProfileService {

  private final UserRepository userRepository;

  public UserProfileService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Get user profile by user ID.
   *
   * @param userId the user ID
   * @return user profile response
   * @throws UserNotFoundException if user is not found
   */
  @Transactional(readOnly = true)
  public UserProfileResponse getUserProfile(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    return mapToProfileResponse(user);
  }

  /**
   * Update user profile.
   *
   * @param userId the user ID
   * @param profileRequest the profile update request
   * @return updated user profile response
   * @throws UserNotFoundException if user is not found
   */
  public UserProfileResponse updateUserProfile(Long userId, UserProfileRequest profileRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    // Update profile fields
    user.setEmail(profileRequest.getEmail());
    user.setFirstName(profileRequest.getFirstName());
    user.setLastName(profileRequest.getLastName());
    user.setPhoneNumber(profileRequest.getPhoneNumber());
    user.setUpdatedAt(LocalDateTime.now());

    User savedUser = userRepository.save(user);
    return mapToProfileResponse(savedUser);
  }

  /**
   * Check if user exists by ID.
   *
   * @param userId the user ID
   * @return true if user exists, false otherwise
   */
  @Transactional(readOnly = true)
  public boolean userExists(Long userId) {
    return userRepository.existsById(userId);
  }

  /**
   * Map User entity to UserProfileResponse DTO.
   *
   * @param user the user entity
   * @return user profile response DTO
   */
  private UserProfileResponse mapToProfileResponse(User user) {
    return new UserProfileResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getPhoneNumber(),
        user.getCreatedAt(),
        user.getUpdatedAt()
    );
  }
}
