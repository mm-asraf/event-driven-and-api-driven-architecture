package com.asraf.architectures.service;

import com.asraf.architectures.model.Order;
import com.asraf.architectures.model.Product;
import com.asraf.architectures.model.common.Status;
import com.asraf.architectures.model.events.InventoryReservedEvent;
import com.asraf.architectures.model.events.OrderCreatedEvent;
import com.asraf.architectures.repository.ProductRepository;
import com.asraf.architectures.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for inventory management and processing.
 * 
 * This service:
 * - Listens to OrderCreatedEvent
 * - Checks product availability
 * - Reserves inventory
 * - Triggers next step in workflow
 * 
 * Similar to Amazon's inventory management where:
 * - Items are reserved when order is placed
 * - Stock is held for a limited time
 * - Payment must be completed to confirm reservation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * EVENT HANDLER: Order Created → Check Inventory
     * 
     * This is the FIRST step in the event chain!
     * Processes OrderCreatedEvent and checks inventory availability.
     */
    @EventListener
    @Async("eventExecutor")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Processing OrderCreatedEvent for OrderId: {}", event.getOrderId());
        
        try {

            Order order = event.getOrder();
            if (order == null) {
                log.error("Order is null in OrderCreatedEvent");
                return;
            }
            
            log.info("Starting inventory check for OrderId: {}, Products: {}", 
                    order.getIdOrderDetails(), order.getProductIds().size());
            boolean inventoryAvailable = checkAndReserveInventory(order);
            
            if (inventoryAvailable) {
                log.info("Inventory reserved successfully for OrderId: {}", order.getIdOrderDetails());

                order.setStatus(Status.INVENTORY_RESERVED);
                orderRepository.save(order);

                List<Long> reservedProductIds = order.getProductIds();
                eventPublisher.publishEvent(new InventoryReservedEvent(this, order, reservedProductIds));
                
                log.info("InventoryReservedEvent published for OrderId: {}", order.getIdOrderDetails());
                
            } else {
                log.warn("Inventory check failed for OrderId: {}", order.getIdOrderDetails());

                order.setStatus(Status.CANCELLED);
                orderRepository.save(order);
                
                log.info("Would send 'out of stock' notification to customer for OrderId: {}", 
                        order.getIdOrderDetails());
            }
            
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent for OrderId: {}, Error: {}", 
                    event.getOrderId(), e.getMessage(), e);

            try {
                Order order = event.getOrder();
                if (order != null) {
                    order.setStatus(Status.CANCELLED);
                    orderRepository.save(order);
                }
            } catch (Exception saveError) {
                log.error("Failed to update order status on error: {}", saveError.getMessage());
            }
        }
    }

    private boolean checkAndReserveInventory(Order order) {
        log.info("Checking inventory for {} products in OrderId: {}", 
                order.getProductIds().size(), order.getIdOrderDetails());
        
        List<Long> reservedProductIds = new ArrayList<>();
        
        try {
            for (Long productId : order.getProductIds()) {
                log.debug("Checking product ID: {}", productId);
                
                Optional<Product> productOpt = productRepository.findById(productId);
                if (productOpt.isEmpty()) {
                    log.error("Product not found - ProductId: {}", productId);
                    return false;
                }
                
                Product product = productOpt.get();
                log.debug("Product found: {} (Stock: {})", product.getName(), product.getStockQuantity());
                

                if (product.getStockQuantity() <= 0) {
                    log.warn("Product out of stock - ProductId: {}, Name: {}", 
                            productId, product.getName());
                    return false;
                }

                int currentStock = product.getStockQuantity();
                product.setStockQuantity(currentStock - 1);
                productRepository.save(product);
                
                reservedProductIds.add(productId);
                log.info("Reserved 1x {} (Stock: {} → {})", 
                        product.getName(), currentStock, currentStock - 1);
                
                // Simulate processing time
                Thread.sleep(100);
            }
            
            log.info("All {} products reserved successfully for OrderId: {}", 
                    reservedProductIds.size(), order.getIdOrderDetails());
            
            return true;
            
        } catch (Exception e) {
            log.error("Error during inventory reservation: {}", e.getMessage(), e);
            
            // Rollback any reservations made
            rollbackInventoryReservations(reservedProductIds);
            
            return false;
        }
    }
    
    /**
     * Rollback inventory reservations in case of error
     */
    private void rollbackInventoryReservations(List<Long> productIds) {
        log.info("Rolling back inventory reservations for {} products", productIds.size());
        
        for (Long productId : productIds) {
            try {
                Optional<Product> productOpt = productRepository.findById(productId);
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    int currentStock = product.getStockQuantity();
                    product.setStockQuantity(currentStock + 1);
                    productRepository.save(product);
                    
                    log.info("Rolled back reservation for {} (Stock: {} → {})", 
                            product.getName(), currentStock, currentStock + 1);
                }
            } catch (Exception e) {
                log.error("Failed to rollback reservation for ProductId: {}, Error: {}", 
                        productId, e.getMessage());
            }
        }
    }
    
    /**
     * Check if a specific product is in stock
     */
    public boolean isProductInStock(Long productId) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                return product.getStockQuantity() > 0;
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking stock for ProductId: {}, Error: {}", productId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get current stock level for a product
     */
    public Integer getProductStockLevel(Long productId) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                return productOpt.get().getStockQuantity();
            }
            return 0;
        } catch (Exception e) {
            log.error("Error getting stock level for ProductId: {}, Error: {}", productId, e.getMessage());
            return 0;
        }
    }
    
    /**
     * Update product stock level
     */
    public boolean updateProductStock(Long productId, Integer newStockLevel) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                int oldStock = product.getStockQuantity();
                product.setStockQuantity(newStockLevel);
                productRepository.save(product);
                
                log.info("Stock updated for {}: {} → {}", 
                        product.getName(), oldStock, newStockLevel);
                
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error updating stock for ProductId: {}, Error: {}", productId, e.getMessage());
            return false;
        }
    }
}