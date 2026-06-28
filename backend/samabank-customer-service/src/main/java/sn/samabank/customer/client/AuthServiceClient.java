package sn.samabank.customer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import sn.samabank.customer.client.dto.UserCreationRequest;
import sn.samabank.customer.client.dto.UserResponse;
import sn.samabank.customer.client.dto.UserStatusUpdateRequest;

import java.util.UUID;

@FeignClient(name = "samabank-auth-service")
public interface AuthServiceClient {

    @PostMapping("/internal/users/create")
    UserResponse createUser(@RequestBody UserCreationRequest request);

    @PatchMapping("/internal/users/{userId}/status")
    void updateUserStatus(@PathVariable("userId") UUID userId,
                          @RequestBody UserStatusUpdateRequest request);
}
