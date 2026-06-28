package sn.samabank.stats.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TellerDashboardStats {
    private long customersCreatedToday;
    private long customersCreatedThisMonth;
    private long accountsOpenedToday;
    private long transactionsProcessedToday;
    private BigDecimal totalVolumeProcessedToday;
    private List<TimeSeriesPoint> myDailyActivity;
    private Map<String, Long> myTransactionsByType;
    private BigDecimal monthlyTarget;
    private BigDecimal monthlyAchieved;
    private double achievementPercentage;
    private List<RecentOperation> recentOperations;
    private List<RecentCustomer> recentCustomers;

    public long getCustomersCreatedToday() { return customersCreatedToday; }
    public void setCustomersCreatedToday(long v) { this.customersCreatedToday = v; }
    public long getCustomersCreatedThisMonth() { return customersCreatedThisMonth; }
    public void setCustomersCreatedThisMonth(long v) { this.customersCreatedThisMonth = v; }
    public long getAccountsOpenedToday() { return accountsOpenedToday; }
    public void setAccountsOpenedToday(long v) { this.accountsOpenedToday = v; }
    public long getTransactionsProcessedToday() { return transactionsProcessedToday; }
    public void setTransactionsProcessedToday(long v) { this.transactionsProcessedToday = v; }
    public BigDecimal getTotalVolumeProcessedToday() { return totalVolumeProcessedToday; }
    public void setTotalVolumeProcessedToday(BigDecimal v) { this.totalVolumeProcessedToday = v; }
    public List<TimeSeriesPoint> getMyDailyActivity() { return myDailyActivity; }
    public void setMyDailyActivity(List<TimeSeriesPoint> v) { this.myDailyActivity = v; }
    public Map<String, Long> getMyTransactionsByType() { return myTransactionsByType; }
    public void setMyTransactionsByType(Map<String, Long> v) { this.myTransactionsByType = v; }
    public BigDecimal getMonthlyTarget() { return monthlyTarget; }
    public void setMonthlyTarget(BigDecimal v) { this.monthlyTarget = v; }
    public BigDecimal getMonthlyAchieved() { return monthlyAchieved; }
    public void setMonthlyAchieved(BigDecimal v) { this.monthlyAchieved = v; }
    public double getAchievementPercentage() { return achievementPercentage; }
    public void setAchievementPercentage(double v) { this.achievementPercentage = v; }
    public List<RecentOperation> getRecentOperations() { return recentOperations; }
    public void setRecentOperations(List<RecentOperation> v) { this.recentOperations = v; }
    public List<RecentCustomer> getRecentCustomers() { return recentCustomers; }
    public void setRecentCustomers(List<RecentCustomer> v) { this.recentCustomers = v; }

    public record TimeSeriesPoint(LocalDate date, long count, BigDecimal amount) {}
    public record RecentOperation(String id, String type, BigDecimal amount, String accountNumber, String customerName, LocalDate date, String status) {}
    public record RecentCustomer(String customerNumber, String fullName, String email, LocalDate createdAt, int accountCount) {}
}
