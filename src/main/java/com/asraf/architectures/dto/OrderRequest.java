package com.asraf.architectures.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for order creation requests.
 * 
 * This represents the data sent by customers when placing an order.
 * Similar to Amazon's checkout form where customers provide:
 * - Product selection
 * - Shipping address
 * - Payment method
 * - Order details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be a positive number")
    private Long userId;
    
    @NotNull(message = "Product list cannot be null")
    @NotEmpty(message = "Product list cannot be empty")
    @Size(min = 1, max = 50, message = "Order must contain between 1 and 50 products")
    private List<Long> productIds;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be at least $0.01")
    @DecimalMax(value = "99999.99", message = "Total amount cannot exceed $99,999.99")
    @Digits(integer = 5, fraction = 2, message = "Total amount must have at most 5 digits before decimal and 2 after")
    private BigDecimal totalAmount;
    
    @NotNull(message = "Shipping address is required")
    @NotBlank(message = "Shipping address cannot be blank")
    @Size(min = 10, max = 500, message = "Shipping address must be between 10 and 500 characters")
    private String shippingAddress;
    
    @NotNull(message = "Payment method is required")
    @NotBlank(message = "Payment method cannot be blank")
    @Pattern(regexp = "^(CREDIT_CARD|DEBIT_CARD|PAYPAL|APPLE_PAY|GOOGLE_PAY)$", 
             message = "Payment method must be one of: CREDIT_CARD, DEBIT_CARD, PAYPAL, APPLE_PAY, GOOGLE_PAY")
    private String paymentMethod;
    
    @Size(max = 1000, message = "Special instructions cannot exceed 1000 characters")
    private String specialInstructions;
    
    @Pattern(regexp = "^(STANDARD|EXPRESS|OVERNIGHT)$", 
             message = "Shipping method must be one of: STANDARD, EXPRESS, OVERNIGHT")
    private String shippingMethod = "STANDARD";
    
    @Email(message = "Email address must be valid")
    @Size(max = 255, message = "Email address cannot exceed 255 characters")
    private String customerEmail;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be a valid international format")
    private String customerPhone;
    
    /**
     * Validate that the total amount matches the expected calculation
     * This is a business rule validation
     */
    @AssertTrue(message = "Total amount validation failed")
    public boolean isTotalAmountValid() {
        if (totalAmount == null || productIds == null || productIds.isEmpty()) {
            return false;
        }
        
        // In a real system, you would calculate the expected total from product prices
        // For now, we'll just ensure it's positive
        return totalAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Get the number of products in the order
     */
    public int getProductCount() {
        return productIds != null ? productIds.size() : 0;
    }
    
    /**
     * Check if the order contains a specific product
     */
    public boolean containsProduct(Long productId) {
        return productIds != null && productIds.contains(productId);
    }
    
    /**
     * Get a formatted display of the payment method
     */
    public String getPaymentMethodDisplay() {
        if (paymentMethod == null) return "Unknown";
        
        return switch (paymentMethod.toUpperCase()) {
            case "CREDIT_CARD" -> "Credit Card";
            case "DEBIT_CARD" -> "Debit Card";
            case "PAYPAL" -> "PayPal";
            case "APPLE_PAY" -> "Apple Pay";
            case "GOOGLE_PAY" -> "Google Pay";
            default -> paymentMethod;
        };
    }
    
    /**
     * Get a formatted display of the shipping method
     */
    public String getShippingMethodDisplay() {
        if (shippingMethod == null) return "Standard";
        
        return switch (shippingMethod.toUpperCase()) {
            case "STANDARD" -> "Standard Shipping (3-5 business days)";
            case "EXPRESS" -> "Express Shipping (1-2 business days)";
            case "OVERNIGHT" -> "Overnight Shipping (Next business day)";
            default -> shippingMethod;
        };
    }
} 