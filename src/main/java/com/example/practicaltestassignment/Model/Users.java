package com.example.practicaltestassignment.Model;

import com.example.practicaltestassignment.Paging_Sorting.Paging;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Users {
    private List<User> items;
    private Paging paging;
}
