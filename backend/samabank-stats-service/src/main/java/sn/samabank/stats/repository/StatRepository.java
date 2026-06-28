package sn.samabank.stats.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import sn.samabank.stats.dto.AdminDashboardStats;
import sn.samabank.stats.dto.CustomerDashboardStats;
import sn.samabank.stats.dto.TellerDashboardStats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class StatRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private LocalDate extractLocalDate(Object dateObj) {
        if (dateObj == null) return null;
        if (dateObj instanceof java.sql.Date) return ((java.sql.Date) dateObj).toLocalDate();
        if (dateObj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate();
        if (dateObj instanceof java.time.Instant) return ((java.time.Instant) dateObj).atZone(ZoneId.systemDefault()).toLocalDate();
        if (dateObj instanceof java.time.OffsetDateTime) return ((java.time.OffsetDateTime) dateObj).toLocalDate();
        if (dateObj instanceof LocalDateTime) return ((LocalDateTime) dateObj).toLocalDate();
        if (dateObj instanceof LocalDate) return (LocalDate) dateObj;
        return LocalDate.parse(dateObj.toString().substring(0, 10));
    }

    public long countCustomers() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM customers").getSingleResult()).longValue();
    }

    public long countUsersByRole(String role) {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM users WHERE role = :role")
            .setParameter("role", role).getSingleResult()).longValue();
    }

    public long countAccounts() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM accounts").getSingleResult()).longValue();
    }

    public long countTransactionsToday() {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM transactions WHERE DATE(executed_at) = CURRENT_DATE")
            .getSingleResult()).longValue();
    }

    public BigDecimal sumAllAccountBalances() {
        Object r = entityManager.createNativeQuery("SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE status = 'ACTIVE'").getSingleResult();
        return r != null ? (BigDecimal) r : BigDecimal.ZERO;
    }

    public BigDecimal sumTransactionVolumeToday() {
        Object r = entityManager.createNativeQuery("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE DATE(executed_at) = CURRENT_DATE AND status = 'COMPLETED'").getSingleResult();
        return r != null ? (BigDecimal) r : BigDecimal.ZERO;
    }

    @SuppressWarnings("unchecked")
    public List<AdminDashboardStats.TimeSeriesPoint> getTransactionTrend(LocalDate from, LocalDate to) {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT DATE(executed_at) as date, COALESCE(SUM(amount), 0) as amount, COUNT(*) as count FROM transactions WHERE DATE(executed_at) BETWEEN :from AND :to GROUP BY DATE(executed_at) ORDER BY date")
            .setParameter("from", from).setParameter("to", to).getResultList();
        return results.stream().map(r -> new AdminDashboardStats.TimeSeriesPoint(
            extractLocalDate(r[0]), (BigDecimal) r[1], ((Number) r[2]).longValue())).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Long> countTransactionsByType(LocalDate from, LocalDate to) {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT type, COUNT(*) FROM transactions WHERE DATE(executed_at) BETWEEN :from AND :to GROUP BY type")
            .setParameter("from", from).setParameter("to", to).getResultList();
        Map<String, Long> map = new HashMap<>();
        for (Object[] r : results) map.put((String) r[0], ((Number) r[1]).longValue());
        return map;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Long> countTransactionsByStatus(LocalDate from, LocalDate to) {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT status, COUNT(*) FROM transactions WHERE DATE(executed_at) BETWEEN :from AND :to GROUP BY status")
            .setParameter("from", from).setParameter("to", to).getResultList();
        Map<String, Long> map = new HashMap<>();
        for (Object[] r : results) map.put((String) r[0], ((Number) r[1]).longValue());
        return map;
    }

    @SuppressWarnings("unchecked")
    public List<AdminDashboardStats.TellerPerformance> getTopTellers(LocalDate from, LocalDate to, int limit) {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT u.username, u.username as full_name, COUNT(t.id) as tx_count, COALESCE(SUM(t.amount), 0) as total_volume FROM users u JOIN transactions t ON t.executed_by = u.id WHERE u.role = 'TELLER' AND DATE(t.executed_at) BETWEEN :from AND :to GROUP BY u.username ORDER BY SUM(t.amount) DESC LIMIT :limit")
            .setParameter("from", from).setParameter("to", to).setParameter("limit", limit).getResultList();
        return results.stream().map(r -> new AdminDashboardStats.TellerPerformance(
            (String) r[0], (String) r[1], ((Number) r[2]).longValue(), (BigDecimal) r[3])).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<AdminDashboardStats.TimeSeriesPoint> getCustomerAcquisitionTrend(LocalDate from, LocalDate to) {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT DATE(created_at) as date, COUNT(*) as count FROM customers WHERE DATE(created_at) BETWEEN :from AND :to GROUP BY DATE(created_at) ORDER BY date")
            .setParameter("from", from).setParameter("to", to).getResultList();
        return results.stream().map(r -> new AdminDashboardStats.TimeSeriesPoint(
            extractLocalDate(r[0]), BigDecimal.ZERO, ((Number) r[1]).longValue())).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<AdminDashboardStats.SecurityAlert> getRecentSecurityAlerts(int limit) {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT event_type, actor_id, payload->>'username' as username, occurred_at as created_at, 'HIGH' as severity FROM audit_events WHERE event_type IN ('LOGIN_FAILED', 'ACCOUNT_LOCKED', 'PASSWORD_CHANGE_FAILED') ORDER BY occurred_at DESC LIMIT :limit")
            .setParameter("limit", limit).getResultList();
        return results.stream().map(r -> new AdminDashboardStats.SecurityAlert(
            (String) r[0], (String) r[2], "Événement de sécurité détecté", extractLocalDate(r[3]), (String) r[4])).collect(Collectors.toList());
    }

    public long countCustomersCreatedBy(UUID tellerId, LocalDate date) {
        return ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(DISTINCT c.id) FROM customers c JOIN accounts a ON a.customer_id = c.id JOIN transactions t ON t.target_account_id = a.id WHERE t.executed_by = :tellerId AND t.type = 'DEPOSIT' AND DATE(t.executed_at) = :date")
            .setParameter("tellerId", tellerId).setParameter("date", date).getSingleResult()).longValue();
    }

    public long countCustomersCreatedByRange(UUID tellerId, LocalDate from, LocalDate to) {
        return ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(DISTINCT c.id) FROM customers c JOIN accounts a ON a.customer_id = c.id JOIN transactions t ON t.target_account_id = a.id WHERE t.executed_by = :tellerId AND t.type = 'DEPOSIT' AND DATE(t.executed_at) BETWEEN :from AND :to")
            .setParameter("tellerId", tellerId).setParameter("from", from).setParameter("to", to).getSingleResult()).longValue();
    }

    public long countAccountsOpenedBy(UUID tellerId, LocalDate date) {
        return ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(DISTINCT a.id) FROM accounts a JOIN transactions t ON t.target_account_id = a.id WHERE t.executed_by = :tellerId AND t.type = 'DEPOSIT' AND DATE(t.executed_at) = :date")
            .setParameter("tellerId", tellerId).setParameter("date", date).getSingleResult()).longValue();
    }

    public long countTransactionsBy(UUID tellerId, LocalDate date) {
        return ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM transactions WHERE executed_by = :tellerId AND DATE(executed_at) = :date")
            .setParameter("tellerId", tellerId).setParameter("date", date).getSingleResult()).longValue();
    }

    public BigDecimal sumVolumeBy(UUID tellerId, LocalDate date) {
        Object r = entityManager.createNativeQuery(
            "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE executed_by = :tellerId AND DATE(executed_at) = :date")
            .setParameter("tellerId", tellerId).setParameter("date", date).getSingleResult();
        return r != null ? (BigDecimal) r : BigDecimal.ZERO;
    }

    public BigDecimal sumVolumeByRange(UUID tellerId, LocalDate from, LocalDate to) {
        Object r = entityManager.createNativeQuery(
            "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE executed_by = :tellerId AND DATE(executed_at) BETWEEN :from AND :to")
            .setParameter("tellerId", tellerId).setParameter("from", from).setParameter("to", to).getSingleResult();
        return r != null ? (BigDecimal) r : BigDecimal.ZERO;
    }

    @SuppressWarnings("unchecked")
    public List<TellerDashboardStats.TimeSeriesPoint> getTellerDailyActivity(UUID tellerId, LocalDate from, LocalDate to) {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT DATE(executed_at) as date, COUNT(*) as count, COALESCE(SUM(amount), 0) as amount FROM transactions WHERE executed_by = :tellerId AND DATE(executed_at) BETWEEN :from AND :to GROUP BY DATE(executed_at) ORDER BY date")
            .setParameter("tellerId", tellerId).setParameter("from", from).setParameter("to", to).getResultList();
        return results.stream().map(r -> new TellerDashboardStats.TimeSeriesPoint(
            extractLocalDate(r[0]), ((Number) r[1]).longValue(), (BigDecimal) r[2])).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Long> countTellerTransactionsByType(UUID tellerId, LocalDate from, LocalDate to) {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT type, COUNT(*) FROM transactions WHERE executed_by = :tellerId AND DATE(executed_at) BETWEEN :from AND :to GROUP BY type")
            .setParameter("tellerId", tellerId).setParameter("from", from).setParameter("to", to).getResultList();
        Map<String, Long> map = new HashMap<>();
        for (Object[] r : results) map.put((String) r[0], ((Number) r[1]).longValue());
        return map;
    }

    @SuppressWarnings("unchecked")
    public List<TellerDashboardStats.RecentOperation> getRecentOperationsByTeller(UUID tellerId, int limit) {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT t.id, t.type, t.amount, a.account_number, CONCAT(c.first_name, ' ', c.last_name), DATE(t.executed_at), t.status FROM transactions t JOIN accounts a ON a.id = COALESCE(t.source_account_id, t.target_account_id) JOIN customers c ON c.id = a.customer_id WHERE t.executed_by = :tellerId ORDER BY t.executed_at DESC LIMIT :limit")
            .setParameter("tellerId", tellerId).setParameter("limit", limit).getResultList();
        return results.stream().map(r -> new TellerDashboardStats.RecentOperation(
            r[0].toString(), (String) r[1], (BigDecimal) r[2], (String) r[3], (String) r[4], extractLocalDate(r[5]), (String) r[6])).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<TellerDashboardStats.RecentCustomer> getRecentCustomersByTeller(UUID tellerId, int limit) {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT c.customer_number, CONCAT(c.first_name, ' ', c.last_name), c.email, DATE(c.created_at), (SELECT COUNT(*) FROM accounts acc WHERE acc.customer_id = c.id) FROM customers c JOIN accounts a ON a.customer_id = c.id JOIN transactions t ON t.target_account_id = a.id WHERE t.executed_by = :tellerId ORDER BY c.created_at DESC LIMIT :limit")
            .setParameter("tellerId", tellerId).setParameter("limit", limit).getResultList();
        return results.stream().map(r -> new TellerDashboardStats.RecentCustomer(
            (String) r[0], (String) r[1], (String) r[2], extractLocalDate(r[3]), ((Number) r[4]).intValue())).collect(Collectors.toList());
    }

    public int countCustomerAccounts(UUID customerUserId) {
        return ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM accounts a JOIN customers c ON c.id = a.customer_id WHERE c.user_id = :userId")
            .setParameter("userId", customerUserId).getSingleResult()).intValue();
    }

    public BigDecimal sumCustomerBalances(UUID customerUserId) {
        Object r = entityManager.createNativeQuery(
            "SELECT COALESCE(SUM(a.balance), 0) FROM accounts a JOIN customers c ON c.id = a.customer_id WHERE c.user_id = :userId AND a.status = 'ACTIVE'")
            .setParameter("userId", customerUserId).getSingleResult();
        return r != null ? (BigDecimal) r : BigDecimal.ZERO;
    }

    public long countCustomerTransactions(UUID customerUserId) {
        return ((Number) entityManager.createNativeQuery(
            "WITH ca AS (SELECT a.id as account_id FROM accounts a JOIN customers c ON c.id = a.customer_id WHERE c.user_id = :userId) SELECT COUNT(*) FROM transactions t WHERE t.source_account_id IN (SELECT account_id FROM ca) OR t.target_account_id IN (SELECT account_id FROM ca)")
            .setParameter("userId", customerUserId).getSingleResult()).longValue();
    }

    public BigDecimal sumCustomerIncoming(UUID customerUserId, LocalDate from, LocalDate to) {
        Object r = entityManager.createNativeQuery(
            "WITH ca AS (SELECT a.id as account_id FROM accounts a JOIN customers c ON c.id = a.customer_id WHERE c.user_id = :userId) SELECT COALESCE(SUM(t.amount), 0) FROM transactions t WHERE t.target_account_id IN (SELECT account_id FROM ca) AND t.type IN ('DEPOSIT', 'TRANSFER') AND DATE(t.executed_at) BETWEEN :from AND :to")
            .setParameter("userId", customerUserId).setParameter("from", from).setParameter("to", to).getSingleResult();
        return r != null ? (BigDecimal) r : BigDecimal.ZERO;
    }

    public BigDecimal sumCustomerOutgoing(UUID customerUserId, LocalDate from, LocalDate to) {
        Object r = entityManager.createNativeQuery(
            "WITH ca AS (SELECT a.id as account_id FROM accounts a JOIN customers c ON c.id = a.customer_id WHERE c.user_id = :userId) SELECT COALESCE(SUM(t.amount), 0) FROM transactions t WHERE t.source_account_id IN (SELECT account_id FROM ca) AND t.type IN ('WITHDRAWAL', 'TRANSFER') AND DATE(t.executed_at) BETWEEN :from AND :to")
            .setParameter("userId", customerUserId).setParameter("from", from).setParameter("to", to).getSingleResult();
        return r != null ? (BigDecimal) r : BigDecimal.ZERO;
    }

    @SuppressWarnings("unchecked")
    public List<CustomerDashboardStats.AccountBalance> getCustomerAccountBalances(UUID customerUserId) {
        List<Object[]> results = entityManager.createNativeQuery(
            "SELECT a.account_number, a.type, a.balance, a.status FROM accounts a JOIN customers c ON c.id = a.customer_id WHERE c.user_id = :userId")
            .setParameter("userId", customerUserId).getResultList();
        return results.stream().map(r -> new CustomerDashboardStats.AccountBalance(
            (String) r[0], (String) r[1], (BigDecimal) r[2], (String) r[3])).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<CustomerDashboardStats.TimeSeriesPoint> getCustomerBalanceHistory(UUID customerUserId, LocalDate from, LocalDate to) {
        List<Object[]> results = entityManager.createNativeQuery(
            "WITH ca AS (SELECT a.id as account_id FROM accounts a JOIN customers c ON c.id = a.customer_id WHERE c.user_id = :userId) SELECT DATE(t.executed_at) as date, SUM(CASE WHEN t.target_account_id IN (SELECT account_id FROM ca) THEN t.amount ELSE -t.amount END) as net FROM transactions t WHERE (t.source_account_id IN (SELECT account_id FROM ca) OR t.target_account_id IN (SELECT account_id FROM ca)) AND DATE(t.executed_at) BETWEEN :from AND :to GROUP BY DATE(t.executed_at) ORDER BY date")
            .setParameter("userId", customerUserId).setParameter("from", from).setParameter("to", to).getResultList();
        return results.stream().map(r -> new CustomerDashboardStats.TimeSeriesPoint(
            extractLocalDate(r[0]), (BigDecimal) r[1])).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public Map<String, BigDecimal> getCustomerExpensesByCategory(UUID customerUserId, LocalDate from, LocalDate to) {
        List<Object[]> results = entityManager.createNativeQuery(
            "WITH ca AS (SELECT a.id as account_id FROM accounts a JOIN customers c ON c.id = a.customer_id WHERE c.user_id = :userId) SELECT t.type, COALESCE(SUM(t.amount), 0) FROM transactions t WHERE t.source_account_id IN (SELECT account_id FROM ca) AND t.type IN ('WITHDRAWAL', 'TRANSFER') AND DATE(t.executed_at) BETWEEN :from AND :to GROUP BY t.type")
            .setParameter("userId", customerUserId).setParameter("from", from).setParameter("to", to).getResultList();
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] r : results) map.put((String) r[0], (BigDecimal) r[1]);
        return map;
    }

    @SuppressWarnings("unchecked")
    public List<CustomerDashboardStats.InOutPoint> getCustomerMonthlyInOut(UUID customerUserId, int months) {
        LocalDateTime startDate = LocalDate.now().minusMonths(months).withDayOfMonth(1).atStartOfDay();
        List<Object[]> results = entityManager.createNativeQuery(
            "WITH ca AS (SELECT a.id as account_id FROM accounts a JOIN customers c ON c.id = a.customer_id WHERE c.user_id = :userId) SELECT TO_CHAR(DATE_TRUNC('month', t.executed_at), 'YYYY-MM') as month, COALESCE(SUM(CASE WHEN t.target_account_id IN (SELECT account_id FROM ca) THEN t.amount ELSE 0 END), 0) as incoming, COALESCE(SUM(CASE WHEN t.source_account_id IN (SELECT account_id FROM ca) THEN t.amount ELSE 0 END), 0) as outgoing FROM transactions t WHERE (t.source_account_id IN (SELECT account_id FROM ca) OR t.target_account_id IN (SELECT account_id FROM ca)) AND t.executed_at >= :startDate GROUP BY DATE_TRUNC('month', t.executed_at) ORDER BY month")
            .setParameter("userId", customerUserId).setParameter("startDate", startDate).getResultList();
        return results.stream().map(r -> new CustomerDashboardStats.InOutPoint(
            (String) r[0], (BigDecimal) r[1], (BigDecimal) r[2])).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<CustomerDashboardStats.RecentTransaction> getCustomerRecentTransactions(UUID customerUserId, int limit) {
        List<Object[]> results = entityManager.createNativeQuery(
            "WITH ca AS (SELECT a.id as account_id FROM accounts a JOIN customers c ON c.id = a.customer_id WHERE c.user_id = :userId) SELECT t.id, t.type, t.amount, sa.account_number as from_acc, ta.account_number as to_acc, t.description, DATE(t.executed_at), t.status FROM transactions t LEFT JOIN accounts sa ON sa.id = t.source_account_id LEFT JOIN accounts ta ON ta.id = t.target_account_id WHERE t.source_account_id IN (SELECT account_id FROM ca) OR t.target_account_id IN (SELECT account_id FROM ca) ORDER BY t.executed_at DESC LIMIT :limit")
            .setParameter("userId", customerUserId).setParameter("limit", limit).getResultList();
        return results.stream().map(r -> new CustomerDashboardStats.RecentTransaction(
            r[0].toString(), (String) r[1], (BigDecimal) r[2], (String) r[3], (String) r[4], (String) r[5], extractLocalDate(r[6]), (String) r[7])).collect(Collectors.toList());
    }
}
