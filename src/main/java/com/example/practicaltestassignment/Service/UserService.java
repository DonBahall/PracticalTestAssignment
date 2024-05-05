package com.example.practicaltestassignment.Service;


import com.example.practicaltestassignment.Model.User;
import com.example.practicaltestassignment.Model.UserSearchCriteria;
import com.example.practicaltestassignment.Model.Users;
import com.example.practicaltestassignment.Paging_Sorting.OrderBy;
import com.example.practicaltestassignment.Paging_Sorting.Paging;
import com.example.practicaltestassignment.Paging_Sorting.SearchPaging;
import com.example.practicaltestassignment.Repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;


import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class UserService {
    @Value("${YEARS}")
    private Integer fromYear;
    private static final String EMAIL_REGEX = "^[\\w\\.-]+@[a-zA-Z\\d\\.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);


    private final UserRepository userRepository;

    private boolean isValidEmail(String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    public ResponseEntity<?> createUser(User user) {
        try {
            if (user == null) {
                return ResponseEntity.badRequest().body("User object is null.");
            }

            if (user.getBirthDate() == null || user.getBirthDate().after(Date.from(Instant.now()))) {
                return ResponseEntity.badRequest().body("Invalid birth date.");
            }

            int age = Period.between(user.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    LocalDate.now()).getYears();
            if (age < fromYear) {
                return ResponseEntity.badRequest().body("User must be at least " + fromYear + " years old.");
            }

            if (!isValidEmail(user.getEmail())) {
                return ResponseEntity.badRequest().body("Invalid email.");
            }

            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

    public ResponseEntity<?> updateUser(Long id, User user) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }
        if (user.getFirstname() != null) {
            existing.setFirstname(user.getFirstname());
        }
        if (user.getLastname() != null) {
            existing.setLastname(user.getLastname());
        }
        if (isValidEmail(user.getEmail())) {
            existing.setEmail(user.getEmail());
        }
        if (user.getPhone() != null) {
            existing.setPhone(user.getPhone());
        }
        if (user.getBirthDate() != null) {
            existing.setBirthDate(user.getBirthDate());
        }
        if (user.getAddress() != null) {
            existing.setAddress(user.getAddress());
        }
        userRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    public Boolean deleteUser(Long id) {
        User userToDelete = userRepository.findById(id).orElse(null);
        if (userToDelete != null) {
            userRepository.delete(userToDelete);
            return true;
        }
        return false;
    }

    public Users getUsers(SearchPaging paging, OrderBy orderBy, UserSearchCriteria searchCriteria) {
        Sort sort = getSort(orderBy);
        if (paging == null) {
            paging = new SearchPaging();
        }
        if (paging.getPage() == null) {
            paging.setPage(0);
        }
        if (paging.getPerPage() == null) {
            paging.setPerPage(15);
        }

        Pageable pageRequest = PageRequest.of(paging.getPage(), paging.getPerPage(), sort);
        Page<User> page;
        if (searchCriteria != null) {
            page = userRepository.findAll(constructSpecification(searchCriteria), pageRequest);
        } else {
            page = userRepository.findAll(pageRequest);
        }
        return new Users(
                page.getContent(),
                new Paging(
                        paging.getPage(),
                        paging.getPerPage(),
                        page.getTotalPages(),
                        (int) page.getTotalElements()
                ));
    }

    private Specification<User> constructSpecification(UserSearchCriteria searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchCriteria.getEmail() != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + searchCriteria.getEmail().toLowerCase() + "%"));
            }
            if (searchCriteria.getBirthDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("birthDate"), searchCriteria.getBirthDateFrom()));
            }
            if (searchCriteria.getBirthDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("birthDate"), searchCriteria.getBirthDateTo()));
            }
            if (searchCriteria.getFirstname() != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstname")), "%" + searchCriteria.getFirstname().toLowerCase() + "%"));

            }
            if (searchCriteria.getPhone() != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), "%" + searchCriteria.getPhone().toLowerCase() + "%"));

            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort getSort(OrderBy orderBy) {
        if (orderBy == null) {
            return Sort.unsorted();
        }
        return switch (orderBy) {
            case IdAsc -> Sort.by("id").ascending();
            case IdDesc -> Sort.by("id").descending();
            case FirstNameAsc -> Sort.by("firstname").ascending();
            case FirstNameDesc -> Sort.by("firstname").descending();
            case DateAsc -> Sort.by("birthDate").ascending();
            case DateDesc -> Sort.by("birthDate").descending();
            default -> Sort.unsorted();
        };
    }
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
