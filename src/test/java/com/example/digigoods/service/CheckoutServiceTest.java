package com.example.digigoods.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.digigoods.dto.CheckoutRequest;
import com.example.digigoods.dto.OrderResponse;
import com.example.digigoods.exception.ExcessiveDiscountException;
import com.example.digigoods.exception.UnauthorizedAccessException;
import com.example.digigoods.model.Discount;
import com.example.digigoods.model.DiscountType;
import com.example.digigoods.model.Product;
import com.example.digigoods.model.User;
import com.example.digigoods.repository.OrderRepository;
import com.example.digigoods.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

  @Mock
  private ProductService productService;

  @Mock
  private DiscountService discountService;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CheckoutService checkoutService;

  private User user;
  private Product product1;
  private Product product2;
  private Discount generalDiscount;
  private CheckoutRequest checkoutRequest;

  @BeforeEach
  void setUp() {
    user = new User(1L, "testuser", "password");
    product1 = new Product(1L, "Product 1", new BigDecimal("100.00"), 10);
    product2 = new Product(2L, "Product 2", new BigDecimal("50.00"), 5);

    generalDiscount = new Discount(1L, "GENERAL20", new BigDecimal("20.00"), DiscountType.GENERAL,
        LocalDate.now().minusDays(1), LocalDate.now().plusDays(30), 10, new HashSet<>());

    checkoutRequest = new CheckoutRequest(1L, List.of(1L, 2L), List.of("GENERAL20"));
  }

  @Test
  @DisplayName("Given valid checkout request, when processing checkout, "
      + "then return success response")
  void givenValidCheckoutRequest_whenProcessingCheckout_thenReturnSuccessResponse() {
    // Arrange
    when(productService.getProductsByIds(anyList())).thenReturn(List.of(product1, product2));
    when(discountService.validateAndGetDiscounts(anyList())).thenReturn(List.of(generalDiscount));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // Act
    OrderResponse response = checkoutService.processCheckout(checkoutRequest, 1L);

    // Assert
    assertEquals("Order created successfully!", response.getMessage());
    assertEquals(new BigDecimal("120.00"), response.getFinalPrice()); // (100 + 50) * 0.8 = 120
    verify(orderRepository).save(any());
    verify(productService).validateAndUpdateStock(anyList());
    verify(discountService).updateDiscountUsage(anyList());
  }

  @Test
  @DisplayName("Given unauthorized user ID, when processing checkout, "
      + "then throw UnauthorizedAccessException")
  void givenUnauthorizedUserId_whenProcessingCheckout_thenThrowUnauthorizedAccessException() {
    // Arrange
    Long authenticatedUserId = 2L; // Different from request user ID

    // Act & Assert
    assertThrows(UnauthorizedAccessException.class,
        () -> checkoutService.processCheckout(checkoutRequest, authenticatedUserId));
  }

  @Test
  @DisplayName("Given excessive discount, when processing checkout, "
      + "then throw ExcessiveDiscountException")
  void givenExcessiveDiscount_whenProcessingCheckout_thenThrowExcessiveDiscountException() {
    // Arrange
    Discount excessiveDiscount = new Discount(2L, "EXCESSIVE80", new BigDecimal("80.00"),
        DiscountType.GENERAL, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30), 10,
        new HashSet<>());

    CheckoutRequest excessiveRequest = new CheckoutRequest(1L, List.of(1L), List.of("EXCESSIVE80"));

    when(productService.getProductsByIds(anyList())).thenReturn(List.of(product1));
    when(discountService.validateAndGetDiscounts(anyList())).thenReturn(List.of(excessiveDiscount));

    // Act & Assert
    assertThrows(ExcessiveDiscountException.class,
        () -> checkoutService.processCheckout(excessiveRequest, 1L));
  }

  @Test
  @DisplayName("Given no discounts, when processing checkout, then return original price")
  void givenNoDiscounts_whenProcessingCheckout_thenReturnOriginalPrice() {
    // Arrange
    CheckoutRequest noDiscountRequest = new CheckoutRequest(1L, List.of(1L), List.of());

    when(productService.getProductsByIds(anyList())).thenReturn(List.of(product1));
    when(discountService.validateAndGetDiscounts(anyList())).thenReturn(List.of());
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // Act
    OrderResponse response = checkoutService.processCheckout(noDiscountRequest, 1L);

    // Assert
    assertEquals("Order created successfully!", response.getMessage());
    assertEquals(new BigDecimal("100.00"), response.getFinalPrice());
  }

  @Test
  @DisplayName("Given product-specific discount, when processing checkout, "
      + "then apply discount to applicable products")
  void givenProductSpecificDiscount_whenProcessingCheckout_thenApplyDiscountToApplicableProducts() {
    // Arrange
    HashSet<Product> applicableProducts = new HashSet<>();
    applicableProducts.add(product1); // Only product1 is applicable for the discount

    Discount productSpecificDiscount = new Discount(3L, "PRODUCT10", new BigDecimal("10.00"),
        DiscountType.PRODUCT_SPECIFIC, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
        10, applicableProducts);

    CheckoutRequest productSpecificRequest = new CheckoutRequest(1L, List.of(1L, 2L),
        List.of("PRODUCT10"));

    when(productService.getProductsByIds(anyList())).thenReturn(List.of(product1, product2));
    when(discountService.validateAndGetDiscounts(anyList()))
        .thenReturn(List.of(productSpecificDiscount));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // Act
    OrderResponse response = checkoutService.processCheckout(productSpecificRequest, 1L);

    // Assert
    assertEquals("Order created successfully!", response.getMessage());
    // Product1: 100 - (100 * 0.10) = 90, Product2: 50 (no discount) = Total: 140
    assertEquals(new BigDecimal("140.00"), response.getFinalPrice());
    verify(orderRepository).save(any());
    verify(productService).validateAndUpdateStock(anyList());
    verify(discountService).updateDiscountUsage(anyList());
  }

  @Test
  @DisplayName("Given mixed discount types, when processing checkout, "
      + "then apply product-specific first then general discounts")
  void givenMixedDiscountTypes_whenProcessingCheckout_thenApplyDiscountsInCorrectOrder() {
    // Arrange
    HashSet<Product> applicableProducts = new HashSet<>();
    applicableProducts.add(product1);

    Discount productSpecificDiscount = new Discount(3L, "PRODUCT10", new BigDecimal("10.00"),
        DiscountType.PRODUCT_SPECIFIC, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30),
        10, applicableProducts);

    CheckoutRequest mixedRequest = new CheckoutRequest(1L, List.of(1L, 2L),
        List.of("PRODUCT10", "GENERAL20"));

    when(productService.getProductsByIds(anyList())).thenReturn(List.of(product1, product2));
    when(discountService.validateAndGetDiscounts(anyList())).thenReturn(
        List.of(productSpecificDiscount, generalDiscount));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // Act
    OrderResponse response = checkoutService.processCheckout(mixedRequest, 1L);

    // Assert
    assertEquals("Order created successfully!", response.getMessage());
    // Step 1: Product1: 100 - 10 = 90, Product2: 50 = Subtotal: 140
    // Step 2: General discount: 140 * 0.8 = 112
    assertEquals(new BigDecimal("112.00"), response.getFinalPrice());
  }

  @Test
  @DisplayName("Given user not found, when processing checkout, then throw RuntimeException")
  void givenUserNotFound_whenProcessingCheckout_thenThrowRuntimeException() {
    // Arrange
    when(productService.getProductsByIds(anyList())).thenReturn(List.of(product1));
    when(discountService.validateAndGetDiscounts(anyList())).thenReturn(List.of());
    when(userRepository.findById(1L)).thenReturn(Optional.empty()); // User not found

    CheckoutRequest request = new CheckoutRequest(1L, List.of(1L), List.of());

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> checkoutService.processCheckout(request, 1L));
    assertEquals("User not found", exception.getMessage());
  }
}
