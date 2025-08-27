package com.asraf.architectures.service;

import com.asraf.architectures.model.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service responsible for sending notifications across multiple channels.
 * 
 * This service:
 * - Listens to ALL events in the system
 * - Sends notifications via multiple channels
 * - Provides customer communication
 * - Handles different notification types
 * 
 * Similar to Amazon's notification system where:
 * - Multiple channels are used (email, SMS, push)
 * - Notifications are sent for each step
 * - Customer engagement is maintained throughout the process
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    /**
     * NOTIFICATION 1: Order Created
     * 
     * Sends immediate confirmation when customer places order
     */
    @EventListener
    @Async("eventExecutor")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Processing OrderCreatedEvent notifications for OrderId: {}", event.getOrderId());
        
        try {
            // Simulate notification processing time
            Thread.sleep(200);
            
            // Send email confirmation
            sendEmailNotification(event.getOrderId(), "ORDER_CONFIRMATION", 
                "Your order has been received and is being processed");
            
            // Send SMS confirmation
            sendSMSNotification(event.getOrderId(), "ORDER_CONFIRMATION", 
                "Order #" + event.getOrderId() + " received! We're processing it now.");
            
            // Send push notification
            sendPushNotification(event.getOrderId(), "ORDER_CONFIRMATION", 
                "Order Confirmed", "Your order is being processed");
            
            log.info("OrderCreatedEvent notifications sent successfully for OrderId: {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Error sending OrderCreatedEvent notifications for OrderId: {}, Error: {}", 
                    event.getOrderId(), e.getMessage());
        }
    }
    
    /**
     * NOTIFICATION 2: Inventory Reserved
     * 
     * Notifies customer that items are available and reserved
     */
    @EventListener
    @Async("eventExecutor")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Processing InventoryReservedEvent notifications for OrderId: {}", event.getOrderId());
        
        try {
            Thread.sleep(150);
            
            if (event.isAllProductsReserved()) {
                // All products reserved successfully
                sendEmailNotification(event.getOrderId(), "INVENTORY_RESERVED", 
                    "All items in your order are available and have been reserved");
                
                sendSMSNotification(event.getOrderId(), "INVENTORY_RESERVED", 
                    "Great news! All items are available. Processing payment now.");
                
                sendPushNotification(event.getOrderId(), "INVENTORY_RESERVED", 
                    "Items Reserved", "All your items are available and reserved");
            } else {
                // Some products couldn't be reserved
                sendEmailNotification(event.getOrderId(), "INVENTORY_PARTIAL", 
                    "Some items in your order are currently out of stock");
                
                sendSMSNotification(event.getOrderId(), "INVENTORY_PARTIAL", 
                    "Some items are out of stock. We'll notify you when available.");
            }
            
            log.info("InventoryReservedEvent notifications sent successfully for OrderId: {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Error sending InventoryReservedEvent notifications for OrderId: {}, Error: {}", 
                    event.getOrderId(), e.getMessage());
        }
    }
    
    /**
     * NOTIFICATION 3: Payment Processed
     * 
     * Notifies customer about payment status
     */
    @EventListener
    @Async("eventExecutor")
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("Processing PaymentProcessedEvent notifications for OrderId: {}", event.getOrderId());
        
        try {
            Thread.sleep(180);
            
            if (event.isPaymentSuccessful()) {
                // Payment successful
                sendEmailNotification(event.getOrderId(), "PAYMENT_SUCCESS", 
                    "Payment processed successfully! Your order is being prepared for shipment.");
                
                sendSMSNotification(event.getOrderId(), "PAYMENT_SUCCESS", 
                    "Payment confirmed! Order #" + event.getOrderId() + " is being prepared.");
                
                sendPushNotification(event.getOrderId(), "PAYMENT_SUCCESS", 
                    "Payment Confirmed", "Your order is being prepared for shipment");
                
            } else {
                // Payment failed
                sendEmailNotification(event.getOrderId(), "PAYMENT_FAILED", 
                    "Payment processing failed. Please update your payment method.");
                
                sendSMSNotification(event.getOrderId(), "PAYMENT_FAILED", 
                    "Payment failed for Order #" + event.getOrderId() + ". Please update payment method.");
                
                sendPushNotification(event.getOrderId(), "PAYMENT_FAILED", 
                    "Payment Failed", "Please update your payment method");
            }
            
            log.info("PaymentProcessedEvent notifications sent successfully for OrderId: {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Error sending PaymentProcessedEvent notifications for OrderId: {}, Error: {}", 
                    event.getOrderId(), e.getMessage());
        }
    }
    
    /**
     * NOTIFICATION 4: Order Shipped
     * 
     * Final notification with tracking information
     */
    @EventListener
    @Async("eventExecutor")
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("Processing OrderShippedEvent notifications for OrderId: {}", event.getOrderId());
        
        try {
            Thread.sleep(250);
            
            // Prepare tracking information
            String trackingInfo = String.format("Tracking: %s | Carrier: %s | ETA: %s", 
                event.getTrackingNumber(), 
                event.getCarrierDisplayName(), 
                event.getEstimatedDelivery());
            
            // Send shipping confirmation
            sendEmailNotification(event.getOrderId(), "ORDER_SHIPPED", 
                "Your order has been shipped! " + trackingInfo);
            
            sendSMSNotification(event.getOrderId(), "ORDER_SHIPPED", 
                "Order #" + event.getOrderId() + " shipped! " + trackingInfo);
            
            sendPushNotification(event.getOrderId(), "ORDER_SHIPPED", 
                "Order Shipped", "Your package is on the way! " + trackingInfo);
            
            // Send deep link notification for tracking
            sendDeepLinkNotification(event.getOrderId(), "TRACKING_AVAILABLE", 
                "Track your package", "Click to track your shipment");
            
            log.info("OrderShippedEvent notifications sent successfully for OrderId: {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Error sending OrderShippedEvent notifications for OrderId: {}, Error: {}", 
                    event.getOrderId(), e.getMessage());
        }
    }
    
    /**
     * Send email notification
     */
    private void sendEmailNotification(Long orderId, String type, String message) {
        try {
            log.debug("Sending email notification - OrderId: {}, Type: {}, Message: {}", 
                    orderId, type, message);
            
            // This would integrate with email service (SendGrid, AWS SES, etc.)
            // For now, just logging the action
            
            log.info("Email notification sent - OrderId: {}, Type: {}", orderId, type);
            
        } catch (Exception e) {
            log.error("Failed to send email notification - OrderId: {}, Type: {}, Error: {}", 
                    orderId, type, e.getMessage());
        }
    }
    
    /**
     * Send SMS notification
     */
    private void sendSMSNotification(Long orderId, String type, String message) {
        try {
            log.debug("Sending SMS notification - OrderId: {}, Type: {}, Message: {}", 
                    orderId, type, message);
            
            // This would integrate with SMS service (Twilio, AWS SNS, etc.)
            // For now, just logging the action
            
            log.info("SMS notification sent - OrderId: {}, Type: {}", orderId, type);
            
        } catch (Exception e) {
            log.error("Failed to send SMS notification - OrderId: {}, Type: {}, Error: {}", 
                    orderId, type, e.getMessage());
        }
    }
    
    /**
     * Send push notification
     */
    private void sendPushNotification(Long orderId, String type, String title, String message) {
        try {
            log.debug("Sending push notification - OrderId: {}, Type: {}, Title: {}, Message: {}", 
                    orderId, type, title, message);
            
            // This would integrate with push notification service (Firebase, AWS SNS, etc.)
            // For now, just logging the action
            
            log.info("Push notification sent - OrderId: {}, Type: {}", orderId, type);
            
        } catch (Exception e) {
            log.error("Failed to send push notification - OrderId: {}, Type: {}, Error: {}", 
                    orderId, type, e.getMessage());
        }
    }
    
    /**
     * Send deep link notification for app navigation
     */
    private void sendDeepLinkNotification(Long orderId, String type, String title, String message) {
        try {
            log.debug("Sending deep link notification - OrderId: {}, Type: {}, Title: {}, Message: {}", 
                    orderId, type, title, message);
            
            // This would integrate with mobile app deep linking
            // For now, just logging the action
            
            log.info("Deep link notification sent - OrderId: {}, Type: {}", orderId, type);
            
        } catch (Exception e) {
            log.error("Failed to send deep link notification - OrderId: {}, Type: {}, Error: {}", 
                    orderId, type, e.getMessage());
        }
    }
    
    /**
     * Send custom notification (for admin or system use)
     */
    public void sendCustomNotification(Long orderId, String channel, String type, String message) {
        log.info("Sending custom notification - OrderId: {}, Channel: {}, Type: {}, Message: {}", 
                orderId, channel, type, message);
        
        try {
            switch (channel.toUpperCase()) {
                case "EMAIL" -> sendEmailNotification(orderId, type, message);
                case "SMS" -> sendSMSNotification(orderId, type, message);
                case "PUSH" -> sendPushNotification(orderId, type, type, message);
                case "DEEP_LINK" -> sendDeepLinkNotification(orderId, type, type, message);
                default -> log.warn("Unknown notification channel: {}", channel);
            }
            
            log.info("Custom notification sent successfully - OrderId: {}, Channel: {}", orderId, channel);
            
        } catch (Exception e) {
            log.error("Failed to send custom notification - OrderId: {}, Channel: {}, Error: {}", 
                    orderId, channel, e.getMessage());
        }
    }
    
    /**
     * Get notification history for an order
     */
    public Object getNotificationHistory(Long orderId) {
        log.debug("Getting notification history for OrderId: {}", orderId);
        
        try {
            // This would retrieve notification history from database
            // For now, returning placeholder data
            
            log.info("Notification history retrieved for OrderId: {}", orderId);
            return new Object(); // Placeholder - implement based on your needs
            
        } catch (Exception e) {
            log.error("Error getting notification history for OrderId: {}, Error: {}", 
                    orderId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Resend failed notification
     */
    public boolean resendFailedNotification(Long orderId, String notificationId) {
        log.info("Resending failed notification - OrderId: {}, NotificationId: {}", orderId, notificationId);
        
        try {
            // This would retry sending a failed notification
            // For now, simulating successful resend
            
            log.info("Failed notification resent successfully - OrderId: {}, NotificationId: {}", 
                    orderId, notificationId);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to resend notification - OrderId: {}, NotificationId: {}, Error: {}", 
                    orderId, notificationId, e.getMessage());
            return false;
        }
    }
}