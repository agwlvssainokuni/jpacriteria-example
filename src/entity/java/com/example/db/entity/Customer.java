package com.example.db.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Entity
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @ToString.Exclude
    @OneToMany(mappedBy = "customer")
    private List<SalesOrder> salesOrder;
}
