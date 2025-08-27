package com.asraf.architectures.model.events;

import com.asraf.architectures.model.Order;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a customer places a new order.
 * 
 * This event triggers the entire order fulfillment workflow:
 * 1. Inventory reservation
 * 2. Payment processing
 * 3. Shipping preparation
 * 
 * Similar to Amazon's order processing system where:
 * - Customer gets immediate confirmation (API response)
 * - Background systems process the order (Event-driven)
 */
@Slf4j
@Getter
public class OrderCreatedEvent extends ApplicationEvent {
    
    private final String eventId;
    private final Order order;
    private final Instant timestamp;
    private final String correlationId;
    
    public OrderCreatedEvent(Object source, Order order) {
        super(source);
        this.eventId = "ORDER_CREATED_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.order = order;
        this.timestamp = Instant.now();
        this.correlationId = UUID.randomUUID().toString();
        
        log.info("OrderCreatedEvent published - OrderId: {}, UserId: {}, Amount: ${}, Products: {}",
                order.getIdOrderDetails(),
                order.getFkIdUserDetails(),
                order.getTotalAmount(),
                order.getProductIds() != null ? order.getProductIds().size() : 0);
        
        log.debug("Event details - EventId: {}, CorrelationId: {}, Timestamp: {}", 
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
} 