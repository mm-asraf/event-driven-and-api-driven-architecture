package com.asraf.architectures.model;

import com.asraf.architectures.model.common.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_order_details")
    private Long idOrderDetails;

    @Column(name = "fk_id_user_details")
    private Long fkIdUserDetails;

    @Column(name = "fk_id_user_address")
    private Long fkIdAddressDetails;

    private List<Long> productIds;

    @Column(name = "num_total_amount")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "enum_status")
    private Status status;

    @ManyToMany
    @JoinColumn(referencedColumnName = "id_address_details")
    private Address shippingAddress;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id_user_details")
    private User userDetails;

    @Column(name = "ts_created")
    private Timestamp tsCreated;

}
