package com.example.booking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "resources")
public class BookableResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 60)
    private String type;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    protected BookableResource() {
    }

    public BookableResource(String name, String type, String description, boolean available, BigDecimal hourlyRate) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.available = available;
        this.hourlyRate = hourlyRate;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
}