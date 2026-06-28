package sn.samabank.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import sn.samabank.transaction.client.dto.CustomerResponse;

import java.util.UUID;

@FeignClient(name = "samabank-customer-service")
public interface CustomerServiceClient {

    @GetMapping("/internal/customers/by-user/{userId}")
    CustomerResponse getByUserId(@PathVariable("userId") UUID userId);

    @GetMapping("/internal/customers/{id}")
    CustomerResponse getById(@PathVariable("id") UUID id);
}
