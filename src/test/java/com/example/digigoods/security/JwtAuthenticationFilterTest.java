package com.example.digigoods.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.digigoods.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock
  private JwtService jwtService;

  @Mock
  private UserDetailsService userDetailsService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    userDetails = new User("testuser", "password", new ArrayList<>());
  }

  @Test
  @DisplayName("Given no authorization header, when doFilterInternal is called, "
      + "then continue filter chain without authentication")
  void givenNoAuthorizationHeader_whenDoFilterInternal_thenContinueWithoutAuthentication()
      throws Exception {
    // Arrange
    when(request.getHeader("Authorization")).thenReturn(null);

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(jwtService, never()).extractUsername(anyString());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @DisplayName("Given authorization header without Bearer prefix, when doFilterInternal is called, "
      + "then continue filter chain without authentication")
  void givenAuthHeaderWithoutBearerPrefix_whenDoFilterInternal_thenContinueWithoutAuthentication()
      throws Exception {
    // Arrange
    when(request.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0");

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(jwtService, never()).extractUsername(anyString());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @DisplayName("Given valid JWT token, when doFilterInternal is called, "
      + "then set authentication in security context")
  void givenValidJwtToken_whenDoFilterInternal_thenSetAuthentication()
      throws Exception {
    // Arrange
    String token = "valid.jwt.token";
    String username = "testuser";
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtService.extractUsername(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    when(jwtService.validateToken(token, username)).thenReturn(true);

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(jwtService).extractUsername(token);
    verify(userDetailsService).loadUserByUsername(username);
    verify(jwtService).validateToken(token, username);

    // Verify authentication is set
    UsernamePasswordAuthenticationToken authentication =
        (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext()
            .getAuthentication();
    assertEquals(userDetails, authentication.getPrincipal());
  }

  @Test
  @DisplayName("Given invalid JWT token, when doFilterInternal is called, "
      + "then continue filter chain without authentication")
  void givenInvalidJwtToken_whenDoFilterInternal_thenContinueWithoutAuthentication()
      throws Exception {
    // Arrange
    String token = "invalid.jwt.token";
    String username = "testuser";
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtService.extractUsername(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    when(jwtService.validateToken(token, username)).thenReturn(false);

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(jwtService).extractUsername(token);
    verify(userDetailsService).loadUserByUsername(username);
    verify(jwtService).validateToken(token, username);

    // Verify authentication is not set
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @DisplayName("Given expired JWT token, when doFilterInternal is called, "
      + "then log error and continue filter chain without authentication")
  void givenExpiredJwtToken_whenDoFilterInternal_thenLogErrorAndContinueWithoutAuth()
      throws Exception {
    // Arrange
    String token = "expired.jwt.token";
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtService.extractUsername(token))
        .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(jwtService).extractUsername(token);
    verify(userDetailsService, never()).loadUserByUsername(anyString());

    // Verify authentication is not set
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @DisplayName("Given malformed JWT token, when doFilterInternal is called, "
      + "then log error and continue filter chain without authentication")
  void givenMalformedJwtToken_whenDoFilterInternal_thenLogErrorAndContinueWithoutAuthentication()
      throws Exception {
    // Arrange
    String token = "malformed.jwt.token";
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtService.extractUsername(token)).thenThrow(new MalformedJwtException("Malformed token"));

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(jwtService).extractUsername(token);
    verify(userDetailsService, never()).loadUserByUsername(anyString());

    // Verify authentication is not set
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @DisplayName("Given JWT token with invalid signature, when doFilterInternal is called, "
      + "then log error and continue filter chain without authentication")
  void givenJwtTokenWithInvalidSignature_whenDoFilterInternal_thenLogErrorAndContinueWithoutAuth()
      throws Exception {
    // Arrange
    String token = "token.with.invalid.signature";
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtService.extractUsername(token)).thenThrow(new SignatureException("Invalid signature"));

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(jwtService).extractUsername(token);
    verify(userDetailsService, never()).loadUserByUsername(anyString());

    // Verify authentication is not set
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @DisplayName("Given JWT token with illegal argument, when doFilterInternal is called, "
      + "then log error and continue filter chain without authentication")
  void givenJwtTokenWithIllegalArgument_whenDoFilterInternal_thenLogErrorAndContinueWithoutAuth()
      throws Exception {
    // Arrange
    String token = "token.with.illegal.argument";
    String authHeader = "Bearer " + token;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtService.extractUsername(token))
        .thenThrow(new IllegalArgumentException("Illegal argument"));

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(jwtService).extractUsername(token);
    verify(userDetailsService, never()).loadUserByUsername(anyString());

    // Verify authentication is not set
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @DisplayName("Given valid token but user already authenticated, when doFilterInternal is called, "
      + "then skip authentication and continue filter chain")
  void givenValidTokenButUserAlreadyAuthenticated_whenDoFilterInternal_thenSkipAuth()
      throws Exception {
    // Arrange
    String token = "valid.jwt.token";
    String username = "testuser";
    String authHeader = "Bearer " + token;

    // Set existing authentication
    UsernamePasswordAuthenticationToken existingAuth =
        new UsernamePasswordAuthenticationToken("existinguser", null, new ArrayList<>());
    SecurityContextHolder.getContext().setAuthentication(existingAuth);

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtService.extractUsername(token)).thenReturn(username);

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(jwtService).extractUsername(token);
    verify(userDetailsService, never()).loadUserByUsername(anyString());
    verify(jwtService, never()).validateToken(anyString(), anyString());

    // Verify existing authentication is preserved
    assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
  }
}
