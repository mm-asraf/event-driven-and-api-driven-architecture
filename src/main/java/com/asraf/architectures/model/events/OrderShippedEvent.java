package com.asraf.architectures.model.events;

import com.asraf.architectures.model.Order;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when an order has been shipped to the customer.
 * 
 * This event indicates that:
 * - Order has been packaged and shipped
 * - Tracking information is available
 * - Customer can track their package
 * 
 * Similar to Amazon's shipping system where:
 * - Multiple shipping carriers are supported
 * - Real-time tracking is provided
 * - Delivery estimates are calculated
 */
@Slf4j
@Getter
public class OrderShippedEvent extends ApplicationEvent {
    
    private final String eventId;
    private final Order order;
    private final String trackingNumber;
    private final String shippingCarrier;
    private final String estimatedDelivery;
    private final String shippingMethod;
    private final Instant timestamp;
    private final String correlationId;
    
    public OrderShippedEvent(Object source, Order order, String trackingNumber, 
                           String shippingCarrier, String estimatedDelivery, String shippingMethod) {
        super(source);
        this.eventId = "ORDER_SHIPPED_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.order = order;
        this.trackingNumber = trackingNumber;
        this.shippingCarrier = shippingCarrier;
        this.estimatedDelivery = estimatedDelivery;
        this.shippingMethod = shippingMethod;
        this.timestamp = Instant.now();
        this.correlationId = UUID.randomUUID().toString();
        
        log.info("OrderShippedEvent published - OrderId: {}, Tracking: {}, Carrier: {}, ETA: {}, Method: {}",
                order.getIdOrderDetails(),
                trackingNumber,
                shippingCarrier,
                estimatedDelivery,
                shippingMethod);
        
        log.debug("Shipping details - EventId: {}, CorrelationId: {}, Timestamp: {}", 
                eventId, correlationId, timestamp);
    }

    public Long getOrderId() {
        return order.getIdOrderDetails();
    }

    public Long getUserId() {
        return order.getFkIdUserDetails();
    }

    public String getFormattedAmount() {
        return order.getTotalAmount() != null ? "$" + order.getTotalAmount() : "N/A";
    }

    public boolean hasTrackingNumber() {
        return trackingNumber != null && !trackingNumber.trim().isEmpty();
    }
    

    public boolean hasEstimatedDelivery() {
        return estimatedDelivery != null && !estimatedDelivery.trim().isEmpty();
    }

    public String getCarrierDisplayName() {
        if (shippingCarrier == null) return "Unknown";
        
        return switch (shippingCarrier.toUpperCase()) {
            case "FEDEX" -> "FedEx";
            case "UPS" -> "UPS";
            case "DHL" -> "DHL";
            case "USPS" -> "USPS";
            case "AMAZON" -> "Amazon Logistics";
            default -> shippingCarrier;
        };
    }
} 