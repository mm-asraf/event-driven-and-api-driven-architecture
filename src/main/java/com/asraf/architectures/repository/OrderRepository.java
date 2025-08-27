package com.asraf.architectures.repository;

import com.asraf.architectures.model.Order;
import com.asraf.architectures.model.common.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByFkIdUserDetailsOrderByTsCreatedDesc(Long userId);

    long countByStatus(Status status);

} 