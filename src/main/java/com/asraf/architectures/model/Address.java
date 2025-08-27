package com.asraf.architectures.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "tbl_address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_address_details")
    private Long idAddressDetails;

    @Column(name = "fk_id_user_details")
    private Long fkIdUserDetails;

    @Column(name = "tx_address")
    private String address;

    @Column(name = "nm_city")
    private String city;

    @Column(name = "tx_state")
    private String state;

    @Column(name = "cd_zip")
    private String zipCode;

    @Column(name = "nm_country")
    private String country;

    @Column(name = "ts_created")
    private Timestamp tsCreated;

    @Column(name = "ts_modified")
    private Timestamp tsModified;

}
