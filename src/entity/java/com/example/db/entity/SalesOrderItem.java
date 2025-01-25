package com.example.db.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Data
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ToString.Exclude
    @ManyToOne
    SalesOrder salesOrder;

    @ManyToOne
    Product product;

    BigDecimal unitPrice;

    Long quantity;
}
