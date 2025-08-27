package com.asraf.architectures.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductId implements Serializable {

    @Column(name = "fk_id_order_details")
    private Long orderId;

    @Column(name = "fk_id_product_details")
    private Long productId;
} 