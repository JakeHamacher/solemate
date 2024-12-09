package com.example.application.helpers.repos;

import com.example.application.helpers.Salesperson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalespersonRepository extends JpaRepository<Salesperson, Long> {
    
}
