package sn.samabank.account.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.samabank.account.entity.Account;
import sn.samabank.account.entity.AccountStatus;
import sn.samabank.account.entity.AccountType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    @Query("SELECT a FROM Account a WHERE a.customerId = :customerId ORDER BY a.openedAt DESC")
    List<Account> findAllByCustomerId(@Param("customerId") UUID customerId);

    Optional<Account> findByCustomerIdAndTypeAndStatus(UUID customerId, AccountType type, AccountStatus status);

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumber(String accountNumber);

    long countByStatus(AccountStatus status);

    @Query(value = """
            SELECT a FROM Account a
            WHERE (:status IS NULL OR a.status = :status)
              AND (:type IS NULL OR a.type = :type)
            """,
            countQuery = """
            SELECT COUNT(a) FROM Account a
            WHERE (:status IS NULL OR a.status = :status)
              AND (:type IS NULL OR a.type = :type)
            """)
    Page<Account> findAllWithFilters(
            @Param("status") AccountStatus status,
            @Param("type") AccountType type,
            Pageable pageable);
}
