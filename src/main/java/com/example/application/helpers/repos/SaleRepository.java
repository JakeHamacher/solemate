package com.example.application.helpers.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.application.helpers.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    // You can add custom queries if needed, like find by product ID
}
