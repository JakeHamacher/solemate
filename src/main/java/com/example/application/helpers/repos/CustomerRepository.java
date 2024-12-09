package com.example.application.helpers.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.application.helpers.Customer;

// In CustomerRepository.java
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Existing method for finding by name
    Customer findByName(String name);

    // Add this method to find customer by ID
    Optional<Customer> findById(Long id);
}

