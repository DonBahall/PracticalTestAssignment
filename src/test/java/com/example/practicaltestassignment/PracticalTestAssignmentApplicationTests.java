package com.example.practicaltestassignment;

import com.example.practicaltestassignment.Model.User;
import com.example.practicaltestassignment.Model.UserSearchCriteria;
import com.example.practicaltestassignment.Model.Users;
import com.example.practicaltestassignment.Paging_Sorting.OrderBy;
import com.example.practicaltestassignment.Paging_Sorting.SearchPaging;
import com.example.practicaltestassignment.Repository.UserRepository;
import com.example.practicaltestassignment.Service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PracticalTestAssignmentApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Mock
    private UserRepository userRepository;

    @MockBean
    private UserService userService;
    @Test
    public void testGetUsersEndpoint() throws Exception {
        when(userService.getUsers(any(SearchPaging.class), any(OrderBy.class), any(UserSearchCriteria.class)))
                .thenReturn(new Users(Collections.emptyList(),null));
        mockMvc.perform(get("/getUsers")
                        .param("page", "0")
                        .param("perPage", "10")
                        .param("orderBy", "IdAsc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }
    @Test
    public void testCreateUserEndpoint() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstname("Test");
        when(userService.createUser(any(User.class))).thenReturn(user);
        mockMvc.perform(post("/createUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"test@example.com\", \"firstname\": \"Test\" }"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }


    @Test
    public void testCreateUser_NullUser() {
        User createdUser = userService.createUser(null);
        assertNull(createdUser);
        verifyNoInteractions(userRepository);
    }

    @Test
    public void testCreateUser_BirthDateInFuture() {
        User user = new User();
        user.setBirthDate(Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        User createdUser = userService.createUser(user);

        assertNull(createdUser);
        verifyNoInteractions(userRepository);
    }

    @Test
    public void testCreateUser_InvalidEmail() {
        User user = new User();
        user.setEmail("invalid-email");

        User createdUser = userService.createUser(user);

        assertNull(createdUser);
        verifyNoInteractions(userRepository);
    }

    @Test
    public void testUpdateUserEndpoint() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstname("UpdatedTest");
        when(userService.updateUser(any(User.class))).thenReturn(user);
        mockMvc.perform(put("/updateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"id\": 1, \"email\": \"test@example.com\", \"firstname\": \"UpdatedTest\" }"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstname").value("UpdatedTest"));
    }

    @Test
    public void testDeleteUserEndpoint() throws Exception {
        when(userService.deleteUser(1L)).thenReturn(true);
        mockMvc.perform(delete("/deleteUser")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(true));
    }
}
