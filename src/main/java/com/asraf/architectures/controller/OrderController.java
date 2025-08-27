package com.asraf.architectures.controller;

import com.asraf.architectures.dto.OrderRequest;
import com.asraf.architectures.dto.OrderResponse;
import com.asraf.architectures.model.Order;
import com.asraf.architectures.service.impl.OrderOrchestrationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for order management operations.
 * 
 * This controller demonstrates real-world e-commerce patterns similar to Amazon:
 * - Customer places order (immediate response)
 * - Order tracking and status updates
 * - Order history and management
 * 
 * The API layer provides immediate responses while events handle background processing.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {
    
    private final OrderOrchestrationServiceImpl orderOrchestrationService;
    
    /**
     * 🚀 PLACE ORDER - Main API endpoint
     * 
     * This is the primary endpoint that customers use to place orders.
     * Similar to Amazon's "Buy Now" or "Place Order" functionality.
     * 
     * Flow:
     * 1. Customer submits order (API call)
     * 2. Order is saved to database (immediate response)
     * 3. OrderCreatedEvent is published (background processing starts)
     * 4. Customer gets confirmation while systems process order
     * 
     * @param orderRequest Customer's order details
     * @return Order confirmation with ID and status
     */
    @PostMapping("/place-order")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        log.info("🛒 Order placement request received - UserId: {}, Products: {}, Amount: ${}", 
                orderRequest.getUserId(), 
                orderRequest.getProductCount(), 
                orderRequest.getTotalAmount());
        
        try {
            // This single method call triggers the entire order workflow!
            OrderResponse response = orderOrchestrationService.processCompleteOrder(orderRequest);
            
            log.info("✅ Order placed successfully - OrderId: {}, Status: {}", 
                    response.getOrderId(), response.getStatus());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("❌ Order placement failed - UserId: {}, Error: {}", 
                    orderRequest.getUserId(), e.getMessage(), e);
            
            OrderResponse errorResponse = OrderResponse.createErrorResponse(
                "Order placement failed: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 🔍 GET ORDER STATUS - Real-time order tracking
     * 
     * Customers can check their order status at any time.
     * Similar to Amazon's "Track Package" functionality.
     * 
     * @param orderId Unique order identifier
     * @return Current order status and tracking information
     */
    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> getOrderStatus(@PathVariable Long orderId) {
        log.info("🔍 Order status request - OrderId: {}", orderId);
        
        try {
            Order order = orderOrchestrationService.getOrder(orderId);
            if (order == null) {
                log.warn("⚠️ Order not found - OrderId: {}", orderId);
                return ResponseEntity.notFound().build();
            }
            
            OrderResponse response = OrderResponse.createStatusResponse(
                order.getIdOrderDetails(),
                order.getStatus(),
                order.getTrackingNumber(),
                order.getTsCreated().toLocalDateTime()
            );
            
            log.info("✅ Order status retrieved - OrderId: {}, Status: {}", 
                    orderId, order.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Order status retrieval failed - OrderId: {}, Error: {}", 
                    orderId, e.getMessage(), e);
            
            OrderResponse errorResponse = OrderResponse.createErrorResponse(
                "Failed to retrieve order status: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 📦 GET ORDER DETAILS - Complete order information
     * 
     * Provides full order details including products, shipping, and payment.
     * Similar to Amazon's order confirmation page.
     * 
     * @param orderId Unique order identifier
     * @return Complete order details
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable Long orderId) {
        log.info("📦 Order details request - OrderId: {}", orderId);
        
        try {
            Order order = orderOrchestrationService.getOrder(orderId);
            if (order == null) {
                log.warn("⚠️ Order not found - OrderId: {}", orderId);
                return ResponseEntity.notFound().build();
            }
            
            log.info("✅ Order details retrieved - OrderId: {}, Status: {}, Products: {}", 
                    orderId, order.getStatus(), 
                    order.getProductIds() != null ? order.getProductIds().size() : 0);
            
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            log.error("❌ Order details retrieval failed - OrderId: {}, Error: {}", 
                    orderId, e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 📋 GET USER ORDERS - Order history
     * 
     * Customers can view their complete order history.
     * Similar to Amazon's "Your Orders" page.
     * 
     * @param userId Customer's unique identifier
     * @return List of customer's orders
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        log.info("📋 User orders request - UserId: {}", userId);
        
        try {
            List<Order> orders = orderOrchestrationService.getUserOrders(userId);
            
            log.info("✅ User orders retrieved - UserId: {}, OrderCount: {}", 
                    userId, orders.size());
            
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            log.error("❌ User orders retrieval failed - UserId: {}, Error: {}", 
                    userId, e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 🚫 CANCEL ORDER - Order cancellation
     * 
     * Customers can cancel orders that haven't been shipped yet.
     * Similar to Amazon's order cancellation functionality.
     * 
     * @param orderId Unique order identifier
     * @return Cancellation confirmation
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        log.info("🚫 Order cancellation request - OrderId: {}", orderId);
        
        try {
            boolean cancelled = orderOrchestrationService.cancelOrder(orderId);
            
            if (cancelled) {
                log.info("✅ Order cancelled successfully - OrderId: {}", orderId);
                
                OrderResponse response = OrderResponse.builder()
                    .orderId(orderId)
                    .status("CANCELLED")
                    .message("Order cancelled successfully")
                    .build();
                
                return ResponseEntity.ok(response);
            } else {
                log.warn("⚠️ Order cannot be cancelled - OrderId: {}", orderId);
                
                OrderResponse response = OrderResponse.builder()
                    .orderId(orderId)
                    .status("CANCELLATION_FAILED")
                    .message("Order cannot be cancelled at this stage")
                    .build();
                
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("❌ Order cancellation failed - OrderId: {}, Error: {}", 
                    orderId, e.getMessage(), e);
            
            OrderResponse errorResponse = OrderResponse.createErrorResponse(
                "Order cancellation failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 📊 GET ORDER STATISTICS - Business intelligence
     * 
     * Provides order statistics for business analysis.
     * Similar to Amazon's seller dashboard metrics.
     * 
     * @return Order statistics and metrics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Object> getOrderStatistics() {
        log.info("📊 Order statistics request");
        
        try {
            // This would return order statistics, metrics, and KPIs
            // Implementation depends on your business requirements
            
            log.info("✅ Order statistics retrieved successfully");
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("❌ Order statistics retrieval failed - Error: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 