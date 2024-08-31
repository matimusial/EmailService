package com.example.emailservice.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "servicemen")
public class Serviceman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "serviceman_id")
    private Long servicemanID;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    public Long getServicemanID() {
        return servicemanID;
    }

    public void setServicemanID(Long servicemanID) {
        this.servicemanID = servicemanID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
