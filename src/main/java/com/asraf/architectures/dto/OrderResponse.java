package com.asraf.architectures.dto;

import com.asraf.architectures.model.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for order responses.
 * 
 * This represents the data returned to customers after order operations.
 * Similar to Amazon's order confirmation pages where customers see:
 * - Order ID and status
 * - Order details and items
 * - Shipping and tracking information
 * - Payment confirmation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private Long orderId;
    private String status;
    private String message;
    private String trackingNumber;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedDelivery;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String paymentMethod;
    private List<OrderItemResponse> items;
    private String orderNumber;
    private String customerEmail;
    private String customerPhone;
    
    /**
     * Create a success response for order creation
     */
    public static OrderResponse createSuccessResponse(Long orderId, Status status, 
                                                   BigDecimal totalAmount, LocalDateTime orderDate) {
        return OrderResponse.builder()
                .orderId(orderId)
                .status(status.name())
                .message("Order placed successfully! Processing started.")
                .orderDate(orderDate)
                .totalAmount(totalAmount)
                .orderNumber("ORD-" + String.format("%08d", orderId))
                .build();
    }
    
    /**
     * Create an error response
     */
    public static OrderResponse createErrorResponse(String message) {
        return OrderResponse.builder()
                .status("ERROR")
                .message(message)
                .build();
    }
    
    /**
     * Create a status response for order tracking
     */
    public static OrderResponse createStatusResponse(Long orderId, Status status, 
                                                  String trackingNumber, LocalDateTime orderDate) {
        return OrderResponse.builder()
                .orderId(orderId)
                .status(status.name())
                .message("Order status retrieved successfully")
                .trackingNumber(trackingNumber)
                .orderDate(orderDate)
                .orderNumber("ORD-" + String.format("%08d", orderId))
                .build();
    }
    
    /**
     * Check if the order was successful
     */
    public boolean isSuccess() {
        return orderId != null && !"ERROR".equals(status);
    }
    
    /**
     * Check if the order has tracking information
     */
    public boolean hasTracking() {
        return trackingNumber != null && !trackingNumber.trim().isEmpty();
    }
    
    /**
     * Get formatted total amount
     */
    public String getFormattedAmount() {
        return totalAmount != null ? "$" + totalAmount : "N/A";
    }
    
    /**
     * Get formatted order date
     */
    public String getFormattedOrderDate() {
        return orderDate != null ? orderDate.toString() : "N/A";
    }
    
    /**
     * Get formatted estimated delivery
     */
    public String getFormattedEstimatedDelivery() {
        return estimatedDelivery != null ? estimatedDelivery.toString() : "Not available";
    }
    
    /**
     * Get the number of items in the order
     */
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
    
    /**
     * Check if the order is in a final state
     */
    public boolean isFinalState() {
        if (status == null) return false;
        
        return switch (status.toUpperCase()) {
            case "DELIVERED", "CANCELLED", "REFUNDED" -> true;
            default -> false;
        };
    }
    
    /**
     * Check if the order is being processed
     */
    public boolean isProcessing() {
        if (status == null) return false;
        
        return switch (status.toUpperCase()) {
            case "CREATED", "INVENTORY_RESERVED", "PAYMENT_PROCESSING", 
                 "PAYMENT_CONFIRMED", "PREPARING_SHIPMENT", "SHIPPED" -> true;
            default -> false;
        };
    }
} 