package com.asraf.architectures.service;

import com.asraf.architectures.dto.OrderRequest;
import com.asraf.architectures.dto.OrderResponse;
import com.asraf.architectures.model.Order;
import com.asraf.architectures.model.common.Status;
import com.asraf.architectures.model.events.OrderCreatedEvent;
import com.asraf.architectures.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Core service that orchestrates the complete order workflow.
 * 
 * This service bridges the API-driven layer (immediate responses) with the 
 * Event-driven layer (background processing).
 * 
 * Similar to Amazon's order orchestration where:
 * - Customer gets immediate confirmation
 * - Background systems handle complex workflows
 * - Events coordinate between different services
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderOrchestrationService {
    
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * MAIN METHOD: Process complete order workflow
     * 
     * This method:
     * 1. Saves order to database (immediate response)
     * 2. Publishes OrderCreatedEvent (background processing starts)
     * 3. Returns confirmation to customer
     * 
     * @param request Customer's order request
     * @return Order confirmation response
     */
    public OrderResponse processCompleteOrder(OrderRequest request) {
        log.info("Starting complete order processing for UserId: {}, Products: {}", 
                request.getUserId(), request.getProductCount());
        
        try {
            // STEP 1: Create and save order (API-DRIVEN - for immediate response)
            Order order = createOrderFromRequest(request);
            order = orderRepository.save(order);
            
            log.info("Order saved successfully - OrderId: {}, Status: {}", 
                    order.getIdOrderDetails(), order.getStatus());
            
            // STEP 2: Publish OrderCreatedEvent (EVENT-DRIVEN - background processing starts)
            log.info("Publishing OrderCreatedEvent for OrderId: {}", order.getIdOrderDetails());
            eventPublisher.publishEvent(new OrderCreatedEvent(this, order));
            
            // STEP 3: Return immediate response to user (API-DRIVEN)
            OrderResponse response = OrderResponse.createSuccessResponse(
                order.getIdOrderDetails(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getTsCreated().toLocalDateTime()
            );
            
            log.info("Order confirmation sent to customer - OrderId: {}, OrderNumber: {}", 
                    order.getIdOrderDetails(), response.getOrderNumber());
            
            return response;
            
        } catch (Exception e) {
            log.error("Order processing failed - UserId: {}, Error: {}", 
                    request.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process order: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create Order entity from OrderRequest
     */
    private Order createOrderFromRequest(OrderRequest request) {
        Order order = new Order();
        order.setFkIdUserDetails(request.getUserId());
        order.setFkIdAddressDetails(Long.valueOf(request.getShippingAddress()));
        order.setProductIds(request.getProductIds());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(Status.CREATED);
        order.setTsCreated(new Timestamp(System.currentTimeMillis()));
        
        log.debug("Order entity created from request - UserId: {}, Products: {}, Amount: ${}", 
                request.getUserId(), request.getProductCount(), request.getTotalAmount());
        
        return order;
    }
    
    /**
     * Get order by ID (used by API endpoints)
     */
    public Order getOrder(Long orderId) {
        log.debug("Retrieving order by ID: {}", orderId);
        return orderRepository.findById(orderId).orElse(null);
    }
    
    /**
     * Get user orders (used by API endpoints)
     */
    public List<Order> getUserOrders(Long userId) {
        log.debug("Retrieving orders for UserId: {}", userId);
        return orderRepository.findByFkIdUserDetailsOrderByTsCreatedDesc(userId);
    }
    
    /**
     * Update order status (used by event handlers)
     */
    public void updateOrderStatus(Long orderId, Status newStatus) {
        log.info("Updating order status - OrderId: {}, NewStatus: {}", orderId, newStatus);
        
        Order order = getOrder(orderId);
        if (order != null) {
            order.setStatus(newStatus);
            orderRepository.save(order);
            log.info("Order status updated successfully - OrderId: {}, Status: {}", orderId, newStatus);
        } else {
            log.warn("Order not found for status update - OrderId: {}", orderId);
        }
    }
    
    /**
     * Add tracking number to order (used by shipping service)
     */
    public void addTrackingNumber(Long orderId, String trackingNumber) {
        log.info("Adding tracking number to order - OrderId: {}, Tracking: {}", orderId, trackingNumber);
        
        Order order = getOrder(orderId);
        if (order != null) {
            order.setTrackingNumber(trackingNumber);
            orderRepository.save(order);
            log.info("Tracking number added successfully - OrderId: {}, Tracking: {}", orderId, trackingNumber);
        } else {
            log.warn("Order not found for tracking number update - OrderId: {}", orderId);
        }
    }
    
    /**
     * Cancel order (used by API endpoints)
     */
    public boolean cancelOrder(Long orderId) {
        log.info("Attempting to cancel order - OrderId: {}", orderId);
        
        Order order = getOrder(orderId);
        if (order == null) {
            log.warn("Order not found for cancellation - OrderId: {}", orderId);
            return false;
        }
        
        // Check if order can be cancelled
        if (canOrderBeCancelled(order)) {
            order.setStatus(Status.CANCELLED);
            orderRepository.save(order);
            log.info("Order cancelled successfully - OrderId: {}", orderId);
            return true;
        } else {
            log.warn("Order cannot be cancelled at current status - OrderId: {}, Status: {}", 
                    orderId, order.getStatus());
            return false;
        }
    }
    
    /**
     * Check if order can be cancelled
     */
    private boolean canOrderBeCancelled(Order order) {
        // Orders can only be cancelled if they haven't been shipped
        return order.getStatus() == Status.CREATED || 
               order.getStatus() == Status.INVENTORY_RESERVED ||
               order.getStatus() == Status.PAYMENT_PROCESSED ||
               order.getStatus() == Status.PAYMENT_CONFIRMED ||
               order.getStatus() == Status.PREPARING_SHIPMENT;
    }
    
    /**
     * Get order statistics (used by business intelligence)
     */
    public Object getOrderStatistics() {
        log.debug("Retrieving order statistics");
        
        // This would return order statistics, metrics, and KPIs
        // Implementation depends on your business requirements
        // For now, returning basic counts
        
        long totalOrders = orderRepository.count();
        long createdOrders = orderRepository.countByStatus(Status.CREATED);
        long shippedOrders = orderRepository.countByStatus(Status.SHIPPED);
        long cancelledOrders = orderRepository.countByStatus(Status.CANCELLED);
        
        log.info("Order statistics - Total: {}, Created: {}, Shipped: {}, Cancelled: {}", 
                totalOrders, createdOrders, shippedOrders, cancelledOrders);
        
        return new Object();
    }
}