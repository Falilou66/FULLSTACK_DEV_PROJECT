package sn.samabank.customer.client.dto;

public record UserCreationRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String role
) {}
