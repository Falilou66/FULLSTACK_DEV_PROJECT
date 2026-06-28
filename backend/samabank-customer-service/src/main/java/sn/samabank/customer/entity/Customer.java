package sn.samabank.customer.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "customer_number", nullable = false, unique = true, length = 20)
    private String customerNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Customer() {}

    public static Customer create(
            UUID userId,
            String customerNumber,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String email,
            String phone,
            String address) {

        Customer c = new Customer();
        c.userId         = userId;
        c.customerNumber = customerNumber;
        c.firstName      = firstName.trim();
        c.lastName       = lastName.trim();
        c.dateOfBirth    = dateOfBirth;
        c.email          = email.toLowerCase().trim();
        c.phone          = phone;
        c.address        = address;
        c.status         = CustomerStatus.ACTIVE;
        return c;
    }

    public void update(String firstName, String lastName,
                       String phone, String address) {
        this.firstName = firstName.trim();
        this.lastName  = lastName.trim();
        this.phone     = phone;
        this.address   = address;
    }

    public void suspend() {
        if (this.status == CustomerStatus.CLOSED) {
            throw new IllegalStateException("Client déjà clôturé");
        }
        this.status = CustomerStatus.SUSPENDED;
    }

    public void reactivate() {
        this.status = CustomerStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == CustomerStatus.ACTIVE;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public UUID getId()               { return id; }
    public UUID getUserId()           { return userId; }
    public String getCustomerNumber() { return customerNumber; }
    public String getFirstName()      { return firstName; }
    public String getLastName()       { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getEmail()          { return email; }
    public String getPhone()          { return phone; }
    public String getAddress()        { return address; }
    public CustomerStatus getStatus() { return status; }
    public Instant getCreatedAt()     { return createdAt; }
    public Instant getUpdatedAt()     { return updatedAt; }
}
