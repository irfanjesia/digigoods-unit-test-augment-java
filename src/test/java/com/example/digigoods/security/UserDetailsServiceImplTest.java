package com.example.digigoods.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.digigoods.model.User;
import com.example.digigoods.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserDetailsServiceImpl userDetailsService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User(1L, "testuser", "password123");
  }

  @Test
  @DisplayName("Given existing username, when loading user by username, then return UserDetails")
  void givenExistingUsername_whenLoadingUserByUsername_thenReturnUserDetails() {
    // Arrange
    String username = "testuser";
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

    // Act
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    // Assert
    assertNotNull(userDetails);
    assertEquals(testUser.getUsername(), userDetails.getUsername());
    assertEquals(testUser.getPassword(), userDetails.getPassword());
    assertNotNull(userDetails.getAuthorities());
    assertTrue(userDetails.getAuthorities().isEmpty()); // No authorities assigned
  }

  @Test
  @DisplayName("Given non-existent username, when loading user by username, "
      + "then throw UsernameNotFoundException")
  void givenNonExistentUsername_whenLoadingUserByUsername_thenThrowUsernameNotFoundException() {
    // Arrange
    String username = "nonexistentuser";
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    // Act & Assert
    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
        () -> userDetailsService.loadUserByUsername(username));
    assertEquals("User not found: " + username, exception.getMessage());
  }
}
