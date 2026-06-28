package sn.samabank.transaction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.samabank.transaction.entity.Transaction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE t.sourceAccountId = :accountId OR t.targetAccountId = :accountId ORDER BY t.executedAt DESC")
    Page<Transaction> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccountId = :accountId OR t.targetAccountId = :accountId) AND t.executedAt BETWEEN :from AND :to ORDER BY t.executedAt DESC")
    Page<Transaction> findByAccountIdAndPeriod(@Param("accountId") UUID accountId, @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    @Query("SELECT t FROM Transaction t ORDER BY t.executedAt DESC")
    Page<Transaction> findAllTransactions(Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.executedBy = :executedBy ORDER BY t.executedAt DESC")
    Page<Transaction> findByExecutedBy(@Param("executedBy") UUID executedBy, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.executedBy = :executedBy AND t.executedAt BETWEEN :from AND :to ORDER BY t.executedAt DESC")
    Page<Transaction> findByExecutedByAndPeriod(@Param("executedBy") UUID executedBy, @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.sourceAccountId IN :accountIds OR t.targetAccountId IN :accountIds ORDER BY t.executedAt DESC")
    Page<Transaction> findByAccountIds(@Param("accountIds") List<UUID> accountIds, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccountId IN :accountIds OR t.targetAccountId IN :accountIds) AND t.executedAt BETWEEN :from AND :to ORDER BY t.executedAt DESC")
    Page<Transaction> findByAccountIdsAndPeriod(@Param("accountIds") List<UUID> accountIds, @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);
}
