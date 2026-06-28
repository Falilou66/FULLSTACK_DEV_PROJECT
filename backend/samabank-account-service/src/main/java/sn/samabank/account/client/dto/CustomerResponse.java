package sn.samabank.account.client.dto;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        UUID userId,
        String customerNumber,
        String firstName,
        String lastName,
        String email,
        String phone,
        String status
) {}
