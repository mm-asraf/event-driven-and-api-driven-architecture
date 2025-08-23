package com.asraf.architectures.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user_Details")
    private Long idUserDetails;

    @Column(name = "nm_user_name")
    private String userName;

    @Column(name = "id_email")
    private String email;

    @Column(name = "nm_first")
    private String firstName;

    @Column(name = "nm_last")
    private String lastName;

    @Column(name = "num_phone")
    private String phoneNumber;

    @Column(name = "fl_active")
    private boolean isActive;

    @Column(name = "ts_created")
    private Timestamp tsCreated;

    @Column(name = "ts_modified")
    private Timestamp tsModified;

}
