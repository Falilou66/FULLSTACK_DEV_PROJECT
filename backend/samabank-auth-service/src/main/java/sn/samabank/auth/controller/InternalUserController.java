package sn.samabank.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.samabank.auth.dto.UserResponse;
import sn.samabank.auth.service.UserService;

import java.util.UUID;

/**
 * Endpoints internes - accessibles sans JWT par les autres services.
 * SecurityConfig autorise /internal/** sans auth.
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        UserResponse response = userService.getById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/teller-count")
    public ResponseEntity<Long> getTellerCount() {
        long count = userService.countByRole(sn.samabank.auth.entity.Role.TELLER);
        return ResponseEntity.ok(count);
    }
}
