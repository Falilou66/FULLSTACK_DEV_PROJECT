package sn.samabank.customer.client.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String role,
        String status
) {}
