package com.example.digigoods.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.digigoods.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private AuthenticationException authException;

  @InjectMocks
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  private StringWriter stringWriter;
  private PrintWriter printWriter;

  @BeforeEach
  void setUp() throws Exception {
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
  }

  @Test
  @DisplayName("Given authentication exception, when commence is called, "
      + "then return unauthorized error response")
  void givenAuthenticationException_whenCommenceIsCalled_thenReturnUnauthorizedErrorResponse()
      throws Exception {
    // Arrange
    String requestUri = "/api/orders";
    when(request.getRequestURI()).thenReturn(requestUri);
    when(response.getWriter()).thenReturn(printWriter);

    // Act
    jwtAuthenticationEntryPoint.commence(request, response, authException);

    // Assert
    verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);

    // Verify the JSON response content
    printWriter.flush();
    String jsonResponse = stringWriter.toString();

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    ErrorResponse errorResponse = objectMapper.readValue(jsonResponse, ErrorResponse.class);

    assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
    assertEquals("Unauthorized", errorResponse.getError());
    assertEquals("JWT token is missing or invalid", errorResponse.getMessage());
    assertEquals(requestUri, errorResponse.getPath());
  }

  @Test
  @DisplayName("Given different request URI, when commence is called, "
      + "then include correct path in error response")
  void givenDifferentRequestUri_whenCommenceIsCalled_thenIncludeCorrectPathInErrorResponse()
      throws Exception {
    // Arrange
    String requestUri = "/api/products";
    when(request.getRequestURI()).thenReturn(requestUri);
    when(response.getWriter()).thenReturn(printWriter);

    // Act
    jwtAuthenticationEntryPoint.commence(request, response, authException);

    // Assert
    verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);

    // Verify the path in JSON response
    printWriter.flush();
    String jsonResponse = stringWriter.toString();

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    ErrorResponse errorResponse = objectMapper.readValue(jsonResponse, ErrorResponse.class);

    assertEquals(requestUri, errorResponse.getPath());
  }
}
