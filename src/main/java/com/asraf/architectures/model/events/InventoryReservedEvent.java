package com.asraf.architectures.model.events;

import com.asraf.architectures.model.Order;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Event published when inventory is successfully reserved for an order.
 * 
 * This event indicates that:
 * - All requested products are available
 * - Stock has been reserved (not yet deducted)
 * - Order can proceed to payment processing
 * 
 * Similar to Amazon's inventory management where:
 * - Items are reserved when order is placed
 * - Stock is held for a limited time
 * - Payment must be completed to confirm reservation
 */
@Slf4j
@Getter
public class InventoryReservedEvent extends ApplicationEvent {
    
    private final String eventId;
    private final Order order;
    private final List<Long> reservedProductIds;
    private final Instant timestamp;
    private final String correlationId;
    private final String reservationCode;
    
    public InventoryReservedEvent(Object source, Order order, List<Long> reservedProductIds) {
        super(source);
        this.eventId = "INVENTORY_RESERVED_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.order = order;
        this.reservedProductIds = reservedProductIds;
        this.timestamp = Instant.now();
        this.correlationId = UUID.randomUUID().toString();
        this.reservationCode = "RES_" + System.currentTimeMillis();
        
        log.info("📦 InventoryReservedEvent published - OrderId: {}, ReservedProducts: {}, ReservationCode: {}",
                order.getIdOrderDetails(),
                reservedProductIds.size(),
                reservationCode);
        
        log.debug("Inventory reservation details - EventId: {}, CorrelationId: {}, Timestamp: {}", 
                eventId, correlationId, timestamp);
    }
    

    public Long getOrderId() {
        return order.getIdOrderDetails();
    }

    public Long getUserId() {
        return order.getFkIdUserDetails();
    }

    public boolean isAllProductsReserved() {
        if (order.getProductIds() == null || reservedProductIds == null) {
            return false;
        }
        return order.getProductIds().size() == reservedProductIds.size();
    }

    public int getReservedProductCount() {
        return reservedProductIds != null ? reservedProductIds.size() : 0;
    }
} 