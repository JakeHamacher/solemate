package com.example.application.helpers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.cache.spi.support.AbstractReadWriteAccess.Item;

import com.example.application.helpers.repos.CustomerRepository;
import com.example.application.helpers.repos.SalespersonRepository;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @ManyToOne
    @JoinColumn(name = "salesperson_id")
    private Salesperson salesperson;

    @ManyToOne
    @JoinColumn(name = "id")  // Foreign key to the Customer's id
    private Customer customer;

    private String ticketName;
    private Double totalPrice;

    @Column(name = "date")
    private LocalDateTime date = LocalDateTime.now();

    // One-to-many relationship with Item
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketItem> ticketItems = new ArrayList<>();

    // Getter and setter for ticketId
    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    // Getter and setter for salesperson
    public Salesperson getSalesperson() {
        return salesperson;
    }

    public void setSalesperson(Salesperson salesperson) {
        this.salesperson = salesperson;
    }

    // Getter and setter for customer
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    // Getter and setter for ticketName
    public String getTicketName() {
        return ticketName;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    // Getter and setter for totalPrice
    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    // Getter and setter for date
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    // Helper method to set customer by ID
    public void setCustomerById(Long customerId, CustomerRepository customerRepository) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found for ID: " + customerId));
        this.customer = customer;
    }

    // Helper method to set salesperson by ID
    public void setSalespersonById(Long salespersonId, SalespersonRepository salespersonRepository) {
        Salesperson salesperson = salespersonRepository.findById(salespersonId)
            .orElseThrow(() -> new IllegalArgumentException("Salesperson not found for ID: " + salespersonId));
        this.salesperson = salesperson;
    }

    // Add an Item to the Ticket
    public void addItem(TicketItem ticketItem) {
        ticketItems.add(ticketItem);
    }

    // Get all Items associated with the Ticket
    public List<TicketItem> getItems() {
        return ticketItems;
    }
}