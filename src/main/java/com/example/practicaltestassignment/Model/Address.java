package com.example.practicaltestassignment.Model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String AddressLine1;
    private String AddressLine2;
    private String postalCode;
}
