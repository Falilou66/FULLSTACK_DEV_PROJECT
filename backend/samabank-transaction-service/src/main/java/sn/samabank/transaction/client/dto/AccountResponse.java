package sn.samabank.transaction.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
    UUID id,
    String accountNumber,
    UUID customerId,
    String type,
    String status,
    BigDecimal balance,
    String currency
) {}
