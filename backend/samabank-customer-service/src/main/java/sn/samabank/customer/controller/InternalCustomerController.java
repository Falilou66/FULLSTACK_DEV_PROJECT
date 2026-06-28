package sn.samabank.customer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.samabank.customer.dto.CustomerResponse;
import sn.samabank.customer.service.CustomerService;

import java.util.UUID;

@RestController
@RequestMapping("/internal/customers")
public class InternalCustomerController {

    private final CustomerService customerService;

    public InternalCustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<CustomerResponse> getByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(customerService.getByUserId(userId));
    }

    @GetMapping("/stats/total-count")
    public ResponseEntity<Long> totalCount() {
        return ResponseEntity.ok(customerService.countAll());
    }

    @GetMapping("/stats/active-count")
    public ResponseEntity<Long> activeCount() {
        return ResponseEntity.ok(customerService.countActive());
    }
}
