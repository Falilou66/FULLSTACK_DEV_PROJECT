package sn.samabank.transaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.samabank.transaction.dto.*;
import sn.samabank.transaction.service.TransactionService;
import sn.samabank.transaction.shared.ApiResponse;
import sn.samabank.transaction.shared.SecurityContextHelper;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction", description = "Depot, retrait et virement")
public class TransactionController {

    private final TransactionService transactionService;
    private final SecurityContextHelper securityHelper;

    public TransactionController(TransactionService transactionService, SecurityContextHelper securityHelper) {
        this.transactionService = transactionService;
        this.securityHelper = securityHelper;
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Effectuer un depot")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositRequest request,
            @AuthenticationPrincipal UUID userId, HttpServletRequest httpRequest) {
        String role = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel = securityHelper.getCurrentChannel();
        String ip = securityHelper.getIpAddress(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(transactionService.deposit(request, userId, role, username, channel, ip)));
    }

    @PostMapping("/withdrawal")
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Effectuer un retrait")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody WithdrawalRequest request,
            @AuthenticationPrincipal UUID userId, HttpServletRequest httpRequest) {
        String role = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel = securityHelper.getCurrentChannel();
        String ip = securityHelper.getIpAddress(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(transactionService.withdraw(request, userId, role, username, channel, ip)));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Effectuer un virement")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UUID userId, HttpServletRequest httpRequest) {
        String role = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel = securityHelper.getCurrentChannel();
        String ip = securityHelper.getIpAddress(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(transactionService.transfer(request, userId, role, username, channel, ip)));
    }

    @GetMapping("/my-accounts")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Historique des transactions de mes comptes")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getMyAccountTransactions(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "executedAt,desc") String sort) {
        return ResponseEntity.ok(ApiResponse.ok(
            transactionService.getMyAccountTransactions(userId, from, to, parsePageable(page, size, sort))));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toutes les transactions (admin)")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAllTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "executedAt,desc") String sort) {
        return ResponseEntity.ok(ApiResponse.ok(
            transactionService.getAllTransactions(from, to, parsePageable(page, size, sort))));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Mes transactions effectuees")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getMyTransactions(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "executedAt,desc") String sort) {
        return ResponseEntity.ok(ApiResponse.ok(
            transactionService.getMyTransactions(userId, from, to, parsePageable(page, size, sort))));
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Historique des transactions d'un compte")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> history(
            @PathVariable UUID accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "executedAt,desc") String sort) {
        return ResponseEntity.ok(ApiResponse.ok(
            transactionService.getHistory(accountId, from, to, parsePageable(page, size, sort))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Detail d'une transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getById(id)));
    }

    private Pageable parsePageable(int page, int size, String sort) {
        Set<String> allowed = Set.of("executedAt", "createdAt", "amount", "type", "status");
        String property = "executedAt";
        Sort.Direction direction = Sort.Direction.DESC;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            if (allowed.contains(parts[0].trim())) property = parts[0].trim();
            if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())) direction = Sort.Direction.ASC;
        }
        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
