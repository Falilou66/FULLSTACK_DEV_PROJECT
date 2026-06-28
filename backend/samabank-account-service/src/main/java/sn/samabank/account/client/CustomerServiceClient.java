package sn.samabank.account.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import sn.samabank.account.client.dto.CustomerResponse;

import java.util.UUID;

@FeignClient(name = "samabank-customer-service")
public interface CustomerServiceClient {

    @GetMapping("/internal/customers/{customerId}")
    CustomerResponse getCustomerById(@PathVariable("customerId") UUID customerId);

    @GetMapping("/internal/customers/by-user/{userId}")
    CustomerResponse getCustomerByUserId(@PathVariable("userId") UUID userId);
}
