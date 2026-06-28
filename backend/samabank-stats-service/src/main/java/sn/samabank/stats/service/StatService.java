package sn.samabank.stats.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.samabank.stats.dto.AdminDashboardStats;
import sn.samabank.stats.dto.CustomerDashboardStats;
import sn.samabank.stats.dto.TellerDashboardStats;
import sn.samabank.stats.repository.StatRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StatService {

    private final StatRepository statRepository;

    public StatService(StatRepository statRepository) {
        this.statRepository = statRepository;
    }

    public AdminDashboardStats getAdminDashboardStats(LocalDate from, LocalDate to) {
        AdminDashboardStats stats = new AdminDashboardStats();
        stats.setTotalCustomers(statRepository.countCustomers());
        stats.setTotalTelllers(statRepository.countUsersByRole("TELLER"));
        stats.setTotalAccounts(statRepository.countAccounts());
        stats.setTotalTransactionsToday(statRepository.countTransactionsToday());
        stats.setTotalBalanceAllAccounts(statRepository.sumAllAccountBalances());
        stats.setTotalVolumeToday(statRepository.sumTransactionVolumeToday());
        stats.setTransactionTrend(statRepository.getTransactionTrend(from, to));
        stats.setTransactionsByType(statRepository.countTransactionsByType(from, to));
        stats.setTransactionsByStatus(statRepository.countTransactionsByStatus(from, to));
        stats.setTopTellers(statRepository.getTopTellers(from, to, 5));
        stats.setCustomerAcquisitionTrend(statRepository.getCustomerAcquisitionTrend(from, to));
        stats.setRecentSecurityAlerts(statRepository.getRecentSecurityAlerts(10));
        return stats;
    }

    public TellerDashboardStats getTellerDashboardStats(UUID tellerId, LocalDate from, LocalDate to) {
        TellerDashboardStats stats = new TellerDashboardStats();
        stats.setCustomersCreatedToday(statRepository.countCustomersCreatedBy(tellerId, LocalDate.now()));
        stats.setCustomersCreatedThisMonth(statRepository.countCustomersCreatedByRange(tellerId, LocalDate.now().withDayOfMonth(1), LocalDate.now()));
        stats.setAccountsOpenedToday(statRepository.countAccountsOpenedBy(tellerId, LocalDate.now()));
        stats.setTransactionsProcessedToday(statRepository.countTransactionsBy(tellerId, LocalDate.now()));
        stats.setTotalVolumeProcessedToday(statRepository.sumVolumeBy(tellerId, LocalDate.now()));
        stats.setMyDailyActivity(statRepository.getTellerDailyActivity(tellerId, from, to));
        stats.setMyTransactionsByType(statRepository.countTellerTransactionsByType(tellerId, from, to));
        BigDecimal target = new BigDecimal("10000000");
        BigDecimal achieved = statRepository.sumVolumeByRange(tellerId, LocalDate.now().withDayOfMonth(1), LocalDate.now());
        stats.setMonthlyTarget(target);
        stats.setMonthlyAchieved(achieved);
        stats.setAchievementPercentage(achieved.divide(target, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).doubleValue());
        stats.setRecentOperations(statRepository.getRecentOperationsByTeller(tellerId, 10));
        stats.setRecentCustomers(statRepository.getRecentCustomersByTeller(tellerId, 10));
        return stats;
    }

    public CustomerDashboardStats getCustomerDashboardStats(UUID customerUserId, LocalDate from, LocalDate to) {
        CustomerDashboardStats stats = new CustomerDashboardStats();
        stats.setTotalAccounts(statRepository.countCustomerAccounts(customerUserId));
        stats.setTotalBalance(statRepository.sumCustomerBalances(customerUserId));
        stats.setTotalTransactions(statRepository.countCustomerTransactions(customerUserId));
        stats.setTotalIncomingThisMonth(statRepository.sumCustomerIncoming(customerUserId, LocalDate.now().withDayOfMonth(1), LocalDate.now()));
        stats.setTotalOutgoingThisMonth(statRepository.sumCustomerOutgoing(customerUserId, LocalDate.now().withDayOfMonth(1), LocalDate.now()));
        stats.setAccountBalances(statRepository.getCustomerAccountBalances(customerUserId));
        stats.setBalanceHistory(statRepository.getCustomerBalanceHistory(customerUserId, from, to));
        stats.setExpensesByCategory(statRepository.getCustomerExpensesByCategory(customerUserId, from, to));
        stats.setMonthlyInOut(statRepository.getCustomerMonthlyInOut(customerUserId, 6));
        stats.setRecentTransactions(statRepository.getCustomerRecentTransactions(customerUserId, 10));
        return stats;
    }
}
