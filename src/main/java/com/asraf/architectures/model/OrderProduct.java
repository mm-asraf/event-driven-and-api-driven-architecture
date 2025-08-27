package com.asraf.architectures.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "tbl_order_product_ids")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProduct implements Serializable {

    @EmbeddedId
    private OrderProductId id;

} 