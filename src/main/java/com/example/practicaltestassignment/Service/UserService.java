package com.example.practicaltestassignment.Service;

import com.example.practicaltestassignment.Model.DateSearch;
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
import org.springframework.stereotype.Service;


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

    public User createUser(User user) {
        if (user != null && user.getBirthDate().before(Date.from(Instant.now()))) {
            if (Period.between(user.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    LocalDate.now()).getYears() >= fromYear) {
                if (isValidEmail(user.getEmail())) {
                    return userRepository.save(user);
                }
            }
        }
        return null;
    }

    public User updateUser(User user) {
        User existing = userRepository.findById(user.getId()).orElse(null);
        if (existing == null) {
            return null;
        }

        existing.setFirstname(user.getFirstname());
        existing.setLastname(user.getLastname());

        if (isValidEmail(user.getEmail())) {
            existing.setEmail(user.getEmail());
        }
        existing.setPhone(user.getPhone());
        existing.setBirthDate(user.getBirthDate());
        existing.setAddress(user.getAddress());

        return userRepository.save(existing);
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
            if (searchCriteria.getBirthDate() != null) {
                DateSearch dateSearch = searchCriteria.getBirthDate();
                if (dateSearch.getFrom() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("birthDate"), dateSearch.getFrom()));
                }
                if (dateSearch.getTo() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("birthDate"), dateSearch.getTo()));
                }
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

}
