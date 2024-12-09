package com.example.application.helpers.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.application.helpers.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByNameAndPrice(String name, double price);
}

