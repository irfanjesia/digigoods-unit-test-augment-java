package com.example.digigoods.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.digigoods.dto.UserProfileRequest;
import com.example.digigoods.dto.UserProfileResponse;
import com.example.digigoods.exception.UserNotFoundException;
import com.example.digigoods.model.User;
import com.example.digigoods.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserProfileService userProfileService;

  private User testUser;
  private UserProfileRequest profileRequest;

  @BeforeEach
  void setUp() {
    // Arrange - Set up test data
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setPassword("password");
    testUser.setEmail("test@example.com");
    testUser.setFirstName("John");
    testUser.setLastName("Doe");
    testUser.setPhoneNumber("123-456-7890");
    testUser.setCreatedAt(LocalDateTime.now().minusDays(1));
    testUser.setUpdatedAt(LocalDateTime.now().minusDays(1));

    profileRequest = new UserProfileRequest();
    profileRequest.setEmail("updated@example.com");
    profileRequest.setFirstName("Jane");
    profileRequest.setLastName("Smith");
    profileRequest.setPhoneNumber("098-765-4321");
  }

  @Test
  @DisplayName("Given valid user ID, when getting user profile, then return user profile response")
  void givenValidUserId_whenGettingUserProfile_thenReturnUserProfileResponse() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // Act
    UserProfileResponse result = userProfileService.getUserProfile(1L);

    // Assert
    assertNotNull(result);
    assertEquals(testUser.getId(), result.getId());
    assertEquals(testUser.getUsername(), result.getUsername());
    assertEquals(testUser.getEmail(), result.getEmail());
    assertEquals(testUser.getFirstName(), result.getFirstName());
    assertEquals(testUser.getLastName(), result.getLastName());
    assertEquals(testUser.getPhoneNumber(), result.getPhoneNumber());
    assertEquals(testUser.getCreatedAt(), result.getCreatedAt());
    assertEquals(testUser.getUpdatedAt(), result.getUpdatedAt());
    verify(userRepository).findById(1L);
  }

  @Test
  @DisplayName("Given invalid user ID, when getting user profile, then throw UserNotFoundException")
  void givenInvalidUserId_whenGettingUserProfile_thenThrowUserNotFoundException() {
    // Arrange
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
        () -> userProfileService.getUserProfile(999L));
    assertEquals("User not found with ID: 999", exception.getMessage());
    verify(userRepository).findById(999L);
  }

  @Test
  @DisplayName("Given valid user ID and request, when updating, then return updated profile")
  void givenValidUserIdAndRequest_whenUpdatingProfile_thenReturnUpdatedProfile() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // Act
    UserProfileResponse result = userProfileService.updateUserProfile(1L, profileRequest);

    // Assert
    assertNotNull(result);
    assertEquals(profileRequest.getEmail(), testUser.getEmail());
    assertEquals(profileRequest.getFirstName(), testUser.getFirstName());
    assertEquals(profileRequest.getLastName(), testUser.getLastName());
    assertEquals(profileRequest.getPhoneNumber(), testUser.getPhoneNumber());
    assertNotNull(testUser.getUpdatedAt());
    verify(userRepository).findById(1L);
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("Given invalid user ID and request, when updating, then throw UserNotFoundException")
  void givenInvalidUserIdAndRequest_whenUpdatingProfile_thenThrowUserNotFoundException() {
    // Arrange
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
        () -> userProfileService.updateUserProfile(999L, profileRequest));
    assertEquals("User not found with ID: 999", exception.getMessage());
    verify(userRepository).findById(999L);
  }

  @Test
  @DisplayName("Given existing user ID, when checking if user exists, then return true")
  void givenExistingUserId_whenCheckingIfUserExists_thenReturnTrue() {
    // Arrange
    when(userRepository.existsById(1L)).thenReturn(true);

    // Act
    boolean result = userProfileService.userExists(1L);

    // Assert
    assertTrue(result);
    verify(userRepository).existsById(1L);
  }

  @Test
  @DisplayName("Given non-existing user ID, when checking if user exists, then return false")
  void givenNonExistingUserId_whenCheckingIfUserExists_thenReturnFalse() {
    // Arrange
    when(userRepository.existsById(999L)).thenReturn(false);

    // Act
    boolean result = userProfileService.userExists(999L);

    // Assert
    assertFalse(result);
    verify(userRepository).existsById(999L);
  }

  @Test
  @DisplayName("Given null request fields, when updating profile, then update with null values")
  void givenNullRequestFields_whenUpdatingProfile_thenUpdateWithNullValues() {
    // Arrange
    UserProfileRequest nullFieldsRequest = new UserProfileRequest();
    nullFieldsRequest.setEmail(null);
    nullFieldsRequest.setFirstName(null);
    nullFieldsRequest.setLastName(null);
    nullFieldsRequest.setPhoneNumber(null);
    
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // Act
    UserProfileResponse result = userProfileService.updateUserProfile(1L, nullFieldsRequest);

    // Assert
    assertNotNull(result);
    assertEquals(null, testUser.getEmail());
    assertEquals(null, testUser.getFirstName());
    assertEquals(null, testUser.getLastName());
    assertEquals(null, testUser.getPhoneNumber());
    verify(userRepository).findById(1L);
    verify(userRepository).save(testUser);
  }
}
