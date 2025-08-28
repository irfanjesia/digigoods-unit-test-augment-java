package com.example.digigoods.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.digigoods.dto.UserProfileRequest;
import com.example.digigoods.model.User;
import com.example.digigoods.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class UserProfileControllerIntegrationTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    // Set up MockMvc
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    // Clear and set up test data
    userRepository.deleteAll();

    // Create test user
    testUser = new User();
    testUser.setUsername("testuser");
    testUser.setPassword("password");
    testUser.setEmail("test@example.com");
    testUser.setFirstName("John");
    testUser.setLastName("Doe");
    testUser.setPhoneNumber("123-456-7890");
    testUser.setCreatedAt(LocalDateTime.now().minusDays(1));
    testUser.setUpdatedAt(LocalDateTime.now().minusDays(1));
    testUser = userRepository.save(testUser);
  }

  @Test
  @DisplayName("Given existing user ID, when getting user profile, then return user profile")
  void givenExistingUserId_whenGettingUserProfile_thenReturnUserProfile() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/users/{userId}/profile", testUser.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testUser.getId()))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.phoneNumber").value("123-456-7890"))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  @DisplayName("Given non-existing user ID, when getting user profile, then return not found")
  void givenNonExistingUserId_whenGettingUserProfile_thenReturnNotFound() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/users/{userId}/profile", 999L)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("User not found with ID: 999"));
  }

  @Test
  @DisplayName("Given valid request, when updating profile, then return updated profile")
  void givenValidRequest_whenUpdatingProfile_thenReturnUpdatedProfile()
      throws Exception {
    // Arrange
    UserProfileRequest profileRequest = new UserProfileRequest();
    profileRequest.setEmail("updated@example.com");
    profileRequest.setFirstName("Jane");
    profileRequest.setLastName("Smith");
    profileRequest.setPhoneNumber("098-765-4321");

    // Act & Assert
    mockMvc.perform(put("/users/{userId}/profile", testUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(profileRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testUser.getId()))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("updated@example.com"))
        .andExpect(jsonPath("$.firstName").value("Jane"))
        .andExpect(jsonPath("$.lastName").value("Smith"))
        .andExpect(jsonPath("$.phoneNumber").value("098-765-4321"))
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  @DisplayName("Given non-existing user ID, when updating user profile, then return not found")
  void givenNonExistingUserId_whenUpdatingUserProfile_thenReturnNotFound()
      throws Exception {
    // Arrange
    UserProfileRequest profileRequest = new UserProfileRequest();
    profileRequest.setEmail("updated@example.com");
    profileRequest.setFirstName("Jane");
    profileRequest.setLastName("Smith");
    profileRequest.setPhoneNumber("098-765-4321");

    // Act & Assert
    mockMvc.perform(put("/users/{userId}/profile", 999L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(profileRequest)))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("User not found with ID: 999"));
  }

  @Test
  @DisplayName("Given invalid email format, when updating user profile, then return bad request")
  void givenInvalidEmailFormat_whenUpdatingUserProfile_thenReturnBadRequest()
      throws Exception {
    // Arrange
    UserProfileRequest profileRequest = new UserProfileRequest();
    profileRequest.setEmail("invalid-email");
    profileRequest.setFirstName("Jane");
    profileRequest.setLastName("Smith");
    profileRequest.setPhoneNumber("098-765-4321");

    // Act & Assert
    mockMvc.perform(put("/users/{userId}/profile", testUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(profileRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Email should be valid"));
  }

  @Test
  @DisplayName("Given too long first name, when updating user profile, then return bad request")
  void givenTooLongFirstName_whenUpdatingUserProfile_thenReturnBadRequest()
      throws Exception {
    // Arrange
    UserProfileRequest profileRequest = new UserProfileRequest();
    profileRequest.setEmail("valid@example.com");
    profileRequest.setFirstName("a".repeat(51)); // Exceeds 50 character limit
    profileRequest.setLastName("Smith");
    profileRequest.setPhoneNumber("098-765-4321");

    // Act & Assert
    mockMvc.perform(put("/users/{userId}/profile", testUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(profileRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("First name should not exceed 50 characters"));
  }

  @Test
  @DisplayName("Given null values in request, when updating profile, then return success")
  void givenNullValuesInRequest_whenUpdatingProfile_thenReturnSuccess()
      throws Exception {
    // Arrange
    UserProfileRequest profileRequest = new UserProfileRequest();
    profileRequest.setEmail(null);
    profileRequest.setFirstName(null);
    profileRequest.setLastName(null);
    profileRequest.setPhoneNumber(null);

    // Act & Assert
    mockMvc.perform(put("/users/{userId}/profile", testUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(profileRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testUser.getId()))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").isEmpty())
        .andExpect(jsonPath("$.firstName").isEmpty())
        .andExpect(jsonPath("$.lastName").isEmpty())
        .andExpect(jsonPath("$.phoneNumber").isEmpty());
  }

  @Test
  @DisplayName("Given empty JSON body, when updating profile, then return success with null values")
  void givenEmptyJsonBody_whenUpdatingProfile_thenReturnSuccessWithNullValues()
      throws Exception {
    // Act & Assert
    mockMvc.perform(put("/users/{userId}/profile", testUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testUser.getId()))
        .andExpect(jsonPath("$.username").value("testuser"));
  }
}
