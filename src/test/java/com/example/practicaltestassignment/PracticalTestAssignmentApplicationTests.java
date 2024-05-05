package com.example.practicaltestassignment;

import com.example.practicaltestassignment.Controller.UserController;
import com.example.practicaltestassignment.Model.User;
import com.example.practicaltestassignment.Model.UserSearchCriteria;
import com.example.practicaltestassignment.Model.Users;
import com.example.practicaltestassignment.Paging_Sorting.OrderBy;
import com.example.practicaltestassignment.Paging_Sorting.SearchPaging;
import com.example.practicaltestassignment.Repository.UserRepository;
import com.example.practicaltestassignment.Service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@AutoConfigureMockMvc
class PracticalTestAssignmentApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserController userController;

    @Test
    public void testGetUsers() {
        // Arrange
        SearchPaging searchPaging = new SearchPaging();
        searchPaging.setPage(1);
        searchPaging.setPerPage(10);
        OrderBy orderBy = OrderBy.IdAsc;

        UserSearchCriteria searchCriteria = new UserSearchCriteria();
        Users result = userController.getUsers(searchPaging, orderBy, searchCriteria);

        assertNotNull(result);
        assertFalse(result.getItems().isEmpty());
    }

    @Test
    public void testCreateUserEndpoint() {
        UserRepository userRepository = mock(UserRepository.class);
        UserController userController = new UserController(userService);
        // Prepare test data
        User validUser = new User();
        validUser.setFirstname("John Doe");
        validUser.setEmail("john@example.com");
        validUser.setBirthDate(Date.from(LocalDate.of(1990, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        User invalidUser = new User();
        invalidUser.setFirstname("John Doe");
        invalidUser.setEmail("invalidemail"); // Invalid email
        invalidUser.setBirthDate(Date.from(LocalDate.now().plus(1, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant())); // Future birth date

        // Mock userRepository behavior
        when(userRepository.save(validUser)).thenReturn(validUser);
        when(userRepository.save(invalidUser)).thenThrow(new RuntimeException()); // Simulate repository save error

        // Test valid user creation
        ResponseEntity<?> responseValid = userController.createUser(validUser);
        assertEquals(HttpStatus.OK, responseValid.getStatusCode());
        assertEquals(validUser, responseValid.getBody());

        // Test invalid user creation - null user
        ResponseEntity<?> responseNullUser = userController.createUser(null);
        assertEquals(HttpStatus.BAD_REQUEST, responseNullUser.getStatusCode());
        assertEquals("User object is null.", responseNullUser.getBody());

        // Test invalid user creation - invalid birthdate
        ResponseEntity<?> responseInvalidBirthDate = userController.createUser(invalidUser);
        assertEquals(HttpStatus.BAD_REQUEST, responseInvalidBirthDate.getStatusCode());
        assertEquals("Invalid birth date.", responseInvalidBirthDate.getBody());

        // Test invalid user creation - invalid email
        invalidUser.setBirthDate(Date.from(LocalDate.of(1990, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant())); // Reset birth date
        ResponseEntity<?> responseInvalidEmail = userController.createUser(invalidUser);
        assertEquals(HttpStatus.BAD_REQUEST, responseInvalidEmail.getStatusCode());
        assertEquals("Invalid email.", responseInvalidEmail.getBody());

        // Test internal server error
        User anotherValidUser = new User();
        anotherValidUser.setFirstname("John Doe");
        anotherValidUser.setEmail("alice@example.com");
        anotherValidUser.setBirthDate(Date.from(LocalDate.of(1985, 5, 5).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        ResponseEntity<?> responseInternalServerError = userController.createUser(anotherValidUser);
        assertEquals(HttpStatus.OK, responseInternalServerError.getStatusCode());
    }
    @Test
    public void testDeleteUser_UserNotFound() {
        Long userId = 1L;
        Boolean result = userController.deleteUser(userId);
        assertFalse(result);
    }
}
