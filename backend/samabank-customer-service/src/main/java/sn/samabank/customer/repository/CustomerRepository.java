package sn.samabank.customer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.samabank.customer.entity.Customer;
import sn.samabank.customer.entity.CustomerStatus;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    boolean existsByEmail(String email);

    @Query(value = """
        SELECT c FROM Customer c
        WHERE (:status IS NULL OR c.status = :status)
          AND (
              :search IS NULL
              OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
              OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
              OR LOWER(c.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
          )
        """,
            countQuery = """
        SELECT COUNT(c) FROM Customer c
        WHERE (:status IS NULL OR c.status = :status)
          AND (
              :search IS NULL
              OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
              OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
              OR LOWER(c.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
          )
        """)
    Page<Customer> findAllWithFilters(
            @Param("status") CustomerStatus status,
            @Param("search") String search,
            Pageable pageable);
}
