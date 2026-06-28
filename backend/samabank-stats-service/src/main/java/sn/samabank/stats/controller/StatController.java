package sn.samabank.stats.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.samabank.stats.dto.AdminDashboardStats;
import sn.samabank.stats.dto.CustomerDashboardStats;
import sn.samabank.stats.dto.TellerDashboardStats;
import sn.samabank.stats.service.StatService;
import sn.samabank.stats.shared.ApiResponse;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stats")
@Tag(name = "Statistics", description = "Tableaux de bord et statistiques")
public class StatController {

    private final StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dashboard Admin")
    public ResponseEntity<ApiResponse<AdminDashboardStats>> adminDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(statService.getAdminDashboardStats(effectiveFrom, effectiveTo)));
    }

    @GetMapping("/teller/dashboard")
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Dashboard Teller")
    public ResponseEntity<ApiResponse<TellerDashboardStats>> tellerDashboard(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(statService.getTellerDashboardStats(userId, effectiveFrom, effectiveTo)));
    }

    @GetMapping("/customer/dashboard")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Dashboard Client")
    public ResponseEntity<ApiResponse<CustomerDashboardStats>> customerDashboard(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(statService.getCustomerDashboardStats(userId, effectiveFrom, effectiveTo)));
    }
}
