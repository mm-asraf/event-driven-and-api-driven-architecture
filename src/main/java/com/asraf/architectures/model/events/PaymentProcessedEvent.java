package com.asraf.architectures.model.events;

import com.asraf.architectures.model.Order;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published when payment processing is completed for an order.
 * 
 * This event indicates that:
 * - Payment has been processed (success or failure)
 * - Transaction details are available
 * - Order can proceed to next step based on payment result
 * 
 * Similar to Amazon's payment processing where:
 * - Multiple payment methods are supported
 * - Payment processing is asynchronous
 * - Order fulfillment depends on payment success
 */
@Slf4j
@Getter
public class PaymentProcessedEvent extends ApplicationEvent {
    
    private final String eventId;
    private final Order order;
    private final boolean paymentSuccessful;
    private final String transactionId;
    private final String paymentMethod;
    private final BigDecimal amount;
    private final String failureReason;
    private final Instant timestamp;
    private final String correlationId;
    
    public PaymentProcessedEvent(Object source, Order order, boolean paymentSuccessful, 
                               String transactionId, String paymentMethod, BigDecimal amount, String failureReason) {
        super(source);
        this.eventId = "PAYMENT_PROCESSED_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.order = order;
        this.paymentSuccessful = paymentSuccessful;
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.failureReason = failureReason;
        this.timestamp = Instant.now();
        this.correlationId = UUID.randomUUID().toString();
        
        if (paymentSuccessful) {
            log.info("PaymentProcessedEvent published - OrderId: {}, Success: true, TransactionId: {}, Method: {}, Amount: ${}",
                    order.getIdOrderDetails(), transactionId, paymentMethod, amount);
        } else {
            log.warn("PaymentProcessedEvent published - OrderId: {}, Success: false, Method: {}, Amount: ${}, Reason: {}",
                    order.getIdOrderDetails(), paymentMethod, amount, failureReason);
        }
        
        log.debug("Payment processing details - EventId: {}, CorrelationId: {}, Timestamp: {}", 
                eventId, correlationId, timestamp);
    }

    public Long getOrderId() {
        return order.getIdOrderDetails();
    }

    public Long getUserId() {
        return order.getFkIdUserDetails();
    }

    public String getFormattedAmount() {
        return amount != null ? "$" + amount : "N/A";
    }

    public boolean isSuccess() {
        return paymentSuccessful;
    }

    public boolean isFailure() {
        return !paymentSuccessful;
    }
} 