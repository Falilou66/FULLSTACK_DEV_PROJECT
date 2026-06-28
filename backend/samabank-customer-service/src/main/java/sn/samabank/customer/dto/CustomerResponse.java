package sn.samabank.customer.dto;

import sn.samabank.customer.entity.Customer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class CustomerResponse {

    private final UUID   id;
    private final UUID   userId;
    private final String customerNumber;
    private final String firstName;
    private final String lastName;
    private final String fullName;
    private final LocalDate dateOfBirth;
    private final String email;
    private final String phone;
    private final String address;
    private final String status;
    private final Instant createdAt;

    private CustomerResponse(Builder builder) {
        this.id             = builder.id;
        this.userId         = builder.userId;
        this.customerNumber = builder.customerNumber;
        this.firstName      = builder.firstName;
        this.lastName       = builder.lastName;
        this.fullName       = builder.fullName;
        this.dateOfBirth    = builder.dateOfBirth;
        this.email          = builder.email;
        this.phone          = builder.phone;
        this.address        = builder.address;
        this.status         = builder.status;
        this.createdAt      = builder.createdAt;
    }

    public static CustomerResponse from(Customer c) {
        return new Builder()
                .id(c.getId())
                .userId(c.getUserId())
                .customerNumber(c.getCustomerNumber())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .fullName(c.getFullName())
                .dateOfBirth(c.getDateOfBirth())
                .email(c.getEmail())
                .phone(c.getPhone())
                .address(c.getAddress())
                .status(c.getStatus().name())
                .createdAt(c.getCreatedAt())
                .build();
    }

    public UUID getId()               { return id; }
    public UUID getUserId()           { return userId; }
    public String getCustomerNumber() { return customerNumber; }
    public String getFirstName()      { return firstName; }
    public String getLastName()       { return lastName; }
    public String getFullName()       { return fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getEmail()          { return email; }
    public String getPhone()          { return phone; }
    public String getAddress()        { return address; }
    public String getStatus()         { return status; }
    public Instant getCreatedAt()     { return createdAt; }

    public static Builder builder()   { return new Builder(); }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private String customerNumber;
        private String firstName;
        private String lastName;
        private String fullName;
        private LocalDate dateOfBirth;
        private String email;
        private String phone;
        private String address;
        private String status;
        private Instant createdAt;

        public Builder id(UUID v)               { this.id = v; return this; }
        public Builder userId(UUID v)           { this.userId = v; return this; }
        public Builder customerNumber(String v) { this.customerNumber = v; return this; }
        public Builder firstName(String v)      { this.firstName = v; return this; }
        public Builder lastName(String v)       { this.lastName = v; return this; }
        public Builder fullName(String v)       { this.fullName = v; return this; }
        public Builder dateOfBirth(LocalDate v) { this.dateOfBirth = v; return this; }
        public Builder email(String v)          { this.email = v; return this; }
        public Builder phone(String v)          { this.phone = v; return this; }
        public Builder address(String v)        { this.address = v; return this; }
        public Builder status(String v)         { this.status = v; return this; }
        public Builder createdAt(Instant v)     { this.createdAt = v; return this; }
        public CustomerResponse build()         { return new CustomerResponse(this); }
    }
}
