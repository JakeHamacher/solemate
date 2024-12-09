package com.example.application.helpers.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.application.helpers.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByName(String name);

    Optional<Customer> findById(Long id);
}

