package sn.samabank.transaction.client.dto;

import java.util.UUID;

public record CustomerResponse(UUID id, UUID userId, String firstName, String lastName, String email, String status) {}
