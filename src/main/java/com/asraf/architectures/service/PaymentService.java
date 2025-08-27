package com.asraf.architectures.service;

import com.asraf.architectures.model.Order;
import com.asraf.architectures.model.common.Status;
import com.asraf.architectures.model.events.InventoryReservedEvent;
import com.asraf.architectures.model.events.PaymentProcessedEvent;
import com.asraf.architectures.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service responsible for payment processing.
 * 
 * This service:
 * - Listens to InventoryReservedEvent
 * - Processes payment for orders
 * - Updates order status
 * - Triggers next step in workflow
 * 
 * Similar to Amazon's payment processing where:
 * - Multiple payment methods are supported
 * - Payment processing is asynchronous
 * - Order fulfillment depends on payment success
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * EVENT HANDLER: Inventory Reserved → Process Payment
     * 
     * This is the SECOND step in the event chain!
     * Processes InventoryReservedEvent and handles payment processing.
     */
    @EventListener
    @Async("eventExecutor")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Processing InventoryReservedEvent for OrderId: {}", event.getOrderId());
        
        try {
            Order order = event.getOrder();
            if (order == null) {
                log.error("Order is null in InventoryReservedEvent");
                return;
            }
            
            log.info("Starting payment processing for OrderId: {}, Amount: ${}", 
                    order.getIdOrderDetails(), order.getTotalAmount());
            
            // Update order status to show payment is being processed
            order.setStatus(Status.PAYMENT_PROCESSED);
            orderRepository.save(order);
            
            // Process the payment
            boolean paymentSuccessful = processPayment(order);
            
            if (paymentSuccessful) {
                log.info("Payment processed successfully for OrderId: {}", order.getIdOrderDetails());
                
                // Update order status
                order.setStatus(Status.PAYMENT_CONFIRMED);
                orderRepository.save(order);
                
                // Publish PaymentProcessedEvent to trigger next step
                String transactionId = generateTransactionId();
                eventPublisher.publishEvent(new PaymentProcessedEvent(
                    this, order, true, transactionId, "CREDIT_CARD", order.getTotalAmount(), null));
                
                log.info("PaymentProcessedEvent published for OrderId: {}", order.getIdOrderDetails());
                
            } else {
                log.warn("Payment processing failed for OrderId: {}", order.getIdOrderDetails());
                
                // Update order status
                order.setStatus(Status.PAYMENT_FAILED);
                orderRepository.save(order);
                
                // Publish failed payment event
                String transactionId = generateTransactionId();
                eventPublisher.publishEvent(new PaymentProcessedEvent(
                    this, order, false, transactionId, "CREDIT_CARD", order.getTotalAmount(), "Payment declined"));
                
                log.info("Would send 'payment failed' notification to customer for OrderId: {}", 
                        order.getIdOrderDetails());
            }
            
        } catch (Exception e) {
            log.error("Error processing InventoryReservedEvent for OrderId: {}, Error: {}", 
                    event.getOrderId(), e.getMessage(), e);
            
            // Update order status to payment failed on error
            try {
                Order order = event.getOrder();
                if (order != null) {
                    order.setStatus(Status.PAYMENT_FAILED);
                    orderRepository.save(order);
                }
            } catch (Exception saveError) {
                log.error("Failed to update order status on error: {}", saveError.getMessage());
            }
        }
    }
    
    /**
     * Process payment for an order
     */
    private boolean processPayment(Order order) {
        log.info("Processing payment for OrderId: {}, Amount: ${}", 
                order.getIdOrderDetails(), order.getTotalAmount());
        
        try {
            // Simulate payment processing steps
            log.debug("   Validating payment method...");
            Thread.sleep(500);
            
            log.debug("   Checking card details...");
            Thread.sleep(300);
            
            log.debug("   Processing transaction...");
            Thread.sleep(800);
            
            log.debug("   Authorizing payment...");
            Thread.sleep(400);
            
            // Simulate payment result (95% success rate for demo)
            boolean paymentSuccessful = Math.random() > 0.05;
            
            if (paymentSuccessful) {
                log.info("Payment authorization successful for OrderId: {}", order.getIdOrderDetails());
            } else {
                log.warn("Payment authorization failed for OrderId: {}", order.getIdOrderDetails());
            }
            
            return paymentSuccessful;
            
        } catch (InterruptedException e) {
            log.error("Payment processing interrupted for OrderId: {}", order.getIdOrderDetails());
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("Payment processing error for OrderId: {}, Error: {}", 
                    order.getIdOrderDetails(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate a unique transaction ID
     */
    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Process payment manually (for testing or admin use)
     */
    public boolean processPaymentManually(Long orderId, String paymentMethod, BigDecimal amount) {
        log.info("Manual payment processing for OrderId: {}, Method: {}, Amount: ${}", 
                orderId, paymentMethod, amount);
        
        try {
            // here you can  integrate with actual payment gateways
            // For now, simulating successful payment
            
            log.info("Manual payment processed successfully for OrderId: {}", orderId);
            return true;
            
        } catch (Exception e) {
            log.error("Manual payment processing failed for OrderId: {}, Error: {}", 
                    orderId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Refund payment for an order
     */
    public boolean refundPayment(Long orderId, BigDecimal refundAmount) {
        log.info("Processing refund for OrderId: {}, Amount: ${}", orderId, refundAmount);
        
        try {
            // This would integrate with actual payment gateways
            // For now, simulating successful refund
        
            log.info("Refund processed successfully for OrderId: {}", orderId);
            return true;
            
        } catch (Exception e) {
            log.error("Refund processing failed for OrderId: {}, Error: {}", 
                    orderId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get payment status for an order
     */
    public String getPaymentStatus(Long orderId) {
        try {
            // This would check actual payment gateway status
            // For now, returning a placeholder
            return "PROCESSED";
        } catch (Exception e) {
            log.error("Error getting payment status for OrderId: {}, Error: {}", 
                    orderId, e.getMessage());
            return "UNKNOWN";
        }
    }
    
    /**
     * Validate payment method
     */
    public boolean validatePaymentMethod(String paymentMethod, String cardNumber) {
        log.debug("Validating payment method: {}", paymentMethod);
        
        try {
            // This would integrate with payment gateway validation
            // For now, basic validation
            
            if (cardNumber == null || cardNumber.length() < 13) {
                return false;
            }

            return isValidCardNumber(cardNumber);
            
        } catch (Exception e) {
            log.error("Error validating payment method: {}", e.getMessage());
            return false;
        }
    }

    private boolean isValidCardNumber(String cardNumber) {
        try {
            int sum = 0;
            boolean alternate = false;
            
            for (int i = cardNumber.length() - 1; i >= 0; i--) {
                int n = Integer.parseInt(cardNumber.substring(i, i + 1));
                if (alternate) {
                    n *= 2;
                    if (n > 9) {
                        n = (n % 10) + 1;
                    }
                }
                sum += n;
                alternate = !alternate;
            }
            
            return (sum % 10 == 0);
        } catch (Exception e) {
            return false;
        }
    }
}