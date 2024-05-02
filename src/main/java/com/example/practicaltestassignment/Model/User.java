package com.example.practicaltestassignment.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String email;
    private String firstname;
    private String lastname;
    private Date birthDate;
    @OneToOne(cascade = CascadeType.ALL)
    private Address address;
    private String phone;

}
