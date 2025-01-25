package com.example.db.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Customer customer;

    @OneToMany(mappedBy = "salesOrder")
    List<SalesOrderItem> item;

    SalesOrderStatus status;
}
