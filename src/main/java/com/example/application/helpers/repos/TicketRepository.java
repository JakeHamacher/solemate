package com.example.application.helpers.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.application.helpers.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
}
