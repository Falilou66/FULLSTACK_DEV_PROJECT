package sn.samabank.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.samabank.auth.dto.CreateUserRequest;
import sn.samabank.auth.dto.UserResponse;
import sn.samabank.auth.entity.Role;
import sn.samabank.auth.entity.User;
import sn.samabank.auth.repository.UserRepository;
import sn.samabank.auth.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

/**
 * Endpoints internes - accessibles sans JWT par les autres services.
 * SecurityConfig autorise /internal/** sans auth.
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InternalUserController(UserService userService,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/create")
    public ResponseEntity<UserResponse> createInternalUser(@RequestBody InternalUserCreateRequest request) {
        String username = generateUsername(request.firstName(), request.lastName());
        Role role = Role.valueOf(request.role().toUpperCase());
        User user = User.create(username, request.email(), passwordEncoder.encode(request.password()), role);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        UserResponse response = userService.getById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/teller-count")
    public ResponseEntity<Long> getTellerCount() {
        long count = userService.countByRole(Role.TELLER);
        return ResponseEntity.ok(count);
    }

    private String generateUsername(String firstName, String lastName) {
        String base = (firstName.substring(0, 1) + lastName).toLowerCase()
                .replaceAll("[^a-z0-9]", "");
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    public record InternalUserCreateRequest(String firstName, String lastName,
                                            String email, String password, String role) {}
}
