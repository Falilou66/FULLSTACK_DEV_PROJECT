package sn.samabank.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import sn.samabank.transaction.client.dto.AccountResponse;
import sn.samabank.transaction.client.dto.AmountBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "samabank-account-service")
public interface AccountServiceClient {

    @GetMapping("/internal/accounts/{id}")
    AccountResponse getById(@PathVariable("id") UUID id);

    @GetMapping("/internal/accounts/by-customer/{customerId}")
    List<AccountResponse> getByCustomerId(@PathVariable("customerId") UUID customerId);

    @PostMapping("/internal/accounts/{id}/debit")
    AccountResponse debit(@PathVariable("id") UUID id, @RequestBody AmountBody body);

    @PostMapping("/internal/accounts/{id}/credit")
    AccountResponse credit(@PathVariable("id") UUID id, @RequestBody AmountBody body);
}
