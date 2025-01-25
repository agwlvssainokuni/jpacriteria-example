package com.example.db.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    BigDecimal unitPrice;

    @ToString.Exclude
    @OneToMany(mappedBy = "product")
    List<SalesOrderItem> salesOrderItem;
}
