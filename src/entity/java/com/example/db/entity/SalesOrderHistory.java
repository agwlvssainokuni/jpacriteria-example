package com.example.db.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        indexes = {
                @Index(columnList = "id")
        }
)
@Data
public class SalesOrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long seq;

    Long id;

    @ManyToOne
    Customer customer;

    SalesOrderStatus status;

    LocalDateTime createdAt;

    public static SalesOrderHistory from(SalesOrder salesOrder) {
        var history = new SalesOrderHistory();
        history.id = salesOrder.getId();
        history.customer = salesOrder.getCustomer();
        history.status = salesOrder.getStatus();
        history.createdAt = LocalDateTime.now();
        return history;
    }
}
