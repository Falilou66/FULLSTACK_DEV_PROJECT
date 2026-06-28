package sn.samabank.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.samabank.account.dto.AccountResponse;
import sn.samabank.account.service.AccountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/accounts")
public class InternalAccountController {

    private final AccountService accountService;

    public InternalAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getById(id));
    }

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getByCustomerId(@PathVariable UUID customerId) {
        return ResponseEntity.ok(accountService.getByCustomerId(customerId));
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<AccountResponse> debit(
            @PathVariable UUID id,
            @RequestBody AmountBody body) {
        return ResponseEntity.ok(accountService.debit(id, body.amount()));
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<AccountResponse> credit(
            @PathVariable UUID id,
            @RequestBody AmountBody body) {
        return ResponseEntity.ok(accountService.credit(id, body.amount()));
    }

    @GetMapping("/stats/total-count")
    public ResponseEntity<Long> totalCount() {
        return ResponseEntity.ok(accountService.countAll());
    }

    @GetMapping("/stats/active-count")
    public ResponseEntity<Long> activeCount() {
        return ResponseEntity.ok(accountService.countActive());
    }

    public record AmountBody(BigDecimal amount) {}
}
