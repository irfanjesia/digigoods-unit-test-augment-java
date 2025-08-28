package com.example.digigoods.exception;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends RuntimeException {

  /**
   * Constructor with message.
   *
   * @param message the error message
   */
  public UserNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructor with user ID.
   *
   * @param userId the user ID that was not found
   */
  public UserNotFoundException(Long userId) {
    super("User not found with ID: " + userId);
  }
}
