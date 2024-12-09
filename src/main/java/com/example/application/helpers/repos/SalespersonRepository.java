package com.example.application.helpers.repos;

import com.example.application.helpers.Salesperson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalespersonRepository extends JpaRepository<Salesperson, Long> {
    // You can add custom queries here if needed, but JpaRepository already provides findById() by default.
}
