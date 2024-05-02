package com.example.practicaltestassignment.Controller;

import com.example.practicaltestassignment.Model.User;
import com.example.practicaltestassignment.Model.UserSearchCriteria;
import com.example.practicaltestassignment.Model.Users;
import com.example.practicaltestassignment.Paging_Sorting.OrderBy;
import com.example.practicaltestassignment.Paging_Sorting.SearchPaging;
import com.example.practicaltestassignment.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/getUsers")
    public Users getUsers(SearchPaging paging, OrderBy orderBy, UserSearchCriteria searchCriteria) {
        return userService.getUsers(paging, orderBy, searchCriteria);
    }
    @PostMapping("/createUser")
    public User createUser(@RequestBody User user){
        return userService.createUser(user);
    }
    @PutMapping("/updateUser")
    public User updateUser(User user){
        return userService.updateUser(user);
    }
    @DeleteMapping("/deleteUser")
    public Boolean deleteUser(Long id){
        return userService.deleteUser(id);
    }
}
