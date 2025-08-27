package com.asraf.architectures.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "tbl_product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product_details")
    private Long idProductDetails;

    @Column(name = "nm_product")
    private String name;

    @Column(name = "num_price")
    private BigDecimal price;

    @Column(name = "num_stock_quantity")
    private Integer stockQuantity;

    @Column(name = "tx_description")
    private String description;
}
