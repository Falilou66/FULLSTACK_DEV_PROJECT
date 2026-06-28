package sn.samabank.stats.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AdminDashboardStats {
    private long totalCustomers;
    private long totalTelllers;
    private long totalAccounts;
    private long totalTransactionsToday;
    private BigDecimal totalBalanceAllAccounts;
    private BigDecimal totalVolumeToday;
    private List<TimeSeriesPoint> transactionTrend;
    private Map<String, Long> transactionsByType;
    private Map<String, Long> transactionsByStatus;
    private List<TellerPerformance> topTellers;
    private List<TimeSeriesPoint> customerAcquisitionTrend;
    private List<SecurityAlert> recentSecurityAlerts;

    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long v) { this.totalCustomers = v; }
    public long getTotalTelllers() { return totalTelllers; }
    public void setTotalTelllers(long v) { this.totalTelllers = v; }
    public long getTotalAccounts() { return totalAccounts; }
    public void setTotalAccounts(long v) { this.totalAccounts = v; }
    public long getTotalTransactionsToday() { return totalTransactionsToday; }
    public void setTotalTransactionsToday(long v) { this.totalTransactionsToday = v; }
    public BigDecimal getTotalBalanceAllAccounts() { return totalBalanceAllAccounts; }
    public void setTotalBalanceAllAccounts(BigDecimal v) { this.totalBalanceAllAccounts = v; }
    public BigDecimal getTotalVolumeToday() { return totalVolumeToday; }
    public void setTotalVolumeToday(BigDecimal v) { this.totalVolumeToday = v; }
    public List<TimeSeriesPoint> getTransactionTrend() { return transactionTrend; }
    public void setTransactionTrend(List<TimeSeriesPoint> v) { this.transactionTrend = v; }
    public Map<String, Long> getTransactionsByType() { return transactionsByType; }
    public void setTransactionsByType(Map<String, Long> v) { this.transactionsByType = v; }
    public Map<String, Long> getTransactionsByStatus() { return transactionsByStatus; }
    public void setTransactionsByStatus(Map<String, Long> v) { this.transactionsByStatus = v; }
    public List<TellerPerformance> getTopTellers() { return topTellers; }
    public void setTopTellers(List<TellerPerformance> v) { this.topTellers = v; }
    public List<TimeSeriesPoint> getCustomerAcquisitionTrend() { return customerAcquisitionTrend; }
    public void setCustomerAcquisitionTrend(List<TimeSeriesPoint> v) { this.customerAcquisitionTrend = v; }
    public List<SecurityAlert> getRecentSecurityAlerts() { return recentSecurityAlerts; }
    public void setRecentSecurityAlerts(List<SecurityAlert> v) { this.recentSecurityAlerts = v; }

    public record TimeSeriesPoint(LocalDate date, BigDecimal amount, long count) {}
    public record TellerPerformance(String username, String fullName, long txCount, BigDecimal totalVolume) {}
    public record SecurityAlert(String eventType, String username, String description, LocalDate date, String severity) {}
}
