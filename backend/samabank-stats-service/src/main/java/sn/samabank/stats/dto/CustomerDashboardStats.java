package sn.samabank.stats.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CustomerDashboardStats {
    private int totalAccounts;
    private BigDecimal totalBalance;
    private long totalTransactions;
    private BigDecimal totalIncomingThisMonth;
    private BigDecimal totalOutgoingThisMonth;
    private List<AccountBalance> accountBalances;
    private List<TimeSeriesPoint> balanceHistory;
    private Map<String, BigDecimal> expensesByCategory;
    private List<InOutPoint> monthlyInOut;
    private List<RecentTransaction> recentTransactions;

    public int getTotalAccounts() { return totalAccounts; }
    public void setTotalAccounts(int v) { this.totalAccounts = v; }
    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal v) { this.totalBalance = v; }
    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long v) { this.totalTransactions = v; }
    public BigDecimal getTotalIncomingThisMonth() { return totalIncomingThisMonth; }
    public void setTotalIncomingThisMonth(BigDecimal v) { this.totalIncomingThisMonth = v; }
    public BigDecimal getTotalOutgoingThisMonth() { return totalOutgoingThisMonth; }
    public void setTotalOutgoingThisMonth(BigDecimal v) { this.totalOutgoingThisMonth = v; }
    public List<AccountBalance> getAccountBalances() { return accountBalances; }
    public void setAccountBalances(List<AccountBalance> v) { this.accountBalances = v; }
    public List<TimeSeriesPoint> getBalanceHistory() { return balanceHistory; }
    public void setBalanceHistory(List<TimeSeriesPoint> v) { this.balanceHistory = v; }
    public Map<String, BigDecimal> getExpensesByCategory() { return expensesByCategory; }
    public void setExpensesByCategory(Map<String, BigDecimal> v) { this.expensesByCategory = v; }
    public List<InOutPoint> getMonthlyInOut() { return monthlyInOut; }
    public void setMonthlyInOut(List<InOutPoint> v) { this.monthlyInOut = v; }
    public List<RecentTransaction> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<RecentTransaction> v) { this.recentTransactions = v; }

    public record AccountBalance(String accountNumber, String type, BigDecimal balance, String status) {}
    public record TimeSeriesPoint(LocalDate date, BigDecimal netAmount) {}
    public record InOutPoint(String month, BigDecimal incoming, BigDecimal outgoing) {}
    public record RecentTransaction(String id, String type, BigDecimal amount, String fromAccount, String toAccount, String description, LocalDate date, String status) {}
}
