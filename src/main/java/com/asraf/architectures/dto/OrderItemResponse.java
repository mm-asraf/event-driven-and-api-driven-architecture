package com.asraf.architectures.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for order item responses.
 * 
 * This represents individual products within an order.
 * Similar to Amazon's order details where customers see:
 * - Product name and description
 * - Quantity and unit price
 * - Item total
 * - Product image and details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    
    private Long productId;
    private String productName;
    private String productDescription;
    private String productImage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal itemTotal;
    private String category;
    private String sku;
    private String brand;
    
    /**
     * Calculate the item total if not provided
     */
    public BigDecimal getCalculatedItemTotal() {
        if (itemTotal != null) {
            return itemTotal;
        }
        
        if (quantity != null && unitPrice != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Get formatted unit price
     */
    public String getFormattedUnitPrice() {
        return unitPrice != null ? "$" + unitPrice : "N/A";
    }
    
    /**
     * Get formatted item total
     */
    public String getFormattedItemTotal() {
        return getCalculatedItemTotal().compareTo(BigDecimal.ZERO) > 0 ? 
               "$" + getCalculatedItemTotal() : "N/A";
    }
    
    /**
     * Check if the item has a valid quantity
     */
    public boolean hasValidQuantity() {
        return quantity != null && quantity > 0;
    }
    
    /**
     * Check if the item has a valid price
     */
    public boolean hasValidPrice() {
        return unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Get a summary description of the item
     */
    public String getItemSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (productName != null) {
            summary.append(productName);
        }
        
        if (quantity != null && quantity > 1) {
            summary.append(" (Qty: ").append(quantity).append(")");
        }
        
        if (brand != null && !brand.trim().isEmpty()) {
            summary.append(" - ").append(brand);
        }
        
        return summary.toString();
    }
} 