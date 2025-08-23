package com.asraf.architectures.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.sql.Timestamp;

public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_address_details")
    private Long idAddressDetails;

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
