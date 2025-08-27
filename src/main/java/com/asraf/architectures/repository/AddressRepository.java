package com.asraf.architectures.repository;

import com.asraf.architectures.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

} 