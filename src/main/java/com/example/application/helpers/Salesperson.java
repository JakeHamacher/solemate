package com.example.application.helpers;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Column;

import java.util.List;

@Entity
public class Salesperson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salesperson_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "salesperson")
    private List<Ticket> tickets;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    @Override
    public String toString() {
        return "Salesperson{id=" + id + ", name='" + name + "'}";
    }

}
