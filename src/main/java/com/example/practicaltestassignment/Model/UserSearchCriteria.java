package com.example.practicaltestassignment.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserSearchCriteria {
    private String email;
    private String firstname;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthDateFrom;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthDateTo;
    private String phone;
}
