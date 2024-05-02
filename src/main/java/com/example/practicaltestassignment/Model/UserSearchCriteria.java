package com.example.practicaltestassignment.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserSearchCriteria {
    private String email;
    private String firstname;
    private DateSearch birthDate;
    private String phone;
}
