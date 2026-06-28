package sn.samabank.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.samabank.auth.dto.CreateUserRequest;
import sn.samabank.auth.dto.UpdateUserRequest;
import sn.samabank.auth.dto.UserResponse;
import sn.samabank.auth.entity.Role;
import sn.samabank.auth.entity.UserStatus;
import sn.samabank.auth.service.UserService;
import sn.samabank.auth.shared.ApiResponse;
import sn.samabank.auth.shared.SecurityContextHelper;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Gestion des utilisateurs - ADMIN uniquement")
public class UserController {

    private final UserService userService;
    private final SecurityContextHelper securityHelper;

    public UserController(UserService userService, SecurityContextHelper securityHelper) {
        this.userService = userService;
        this.securityHelper = securityHelper;
    }

    @PostMapping("/teller")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Creer un teller")
    public ResponseEntity<ApiResponse<UserResponse>> createTeller(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UUID adminId,
            HttpServletRequest httpRequest) {

        String role = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel = securityHelper.getCurrentChannel();
        String ip = securityHelper.getIpAddress(httpRequest);

        UserResponse response = userService.createUser(request, Role.TELLER, adminId, role, username, channel, ip);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Creer un admin")
    public ResponseEntity<ApiResponse<UserResponse>> createAdmin(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UUID adminId,
            HttpServletRequest httpRequest) {

        String role = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel = securityHelper.getCurrentChannel();
        String ip = securityHelper.getIpAddress(httpRequest);

        UserResponse response = userService.createUser(request, Role.ADMIN, adminId, role, username, channel, ip);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister les utilisateurs")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAll(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String search,
            @Parameter(description = "Numero de page", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tri", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Pageable pageable = parsePageable(page, size, sort);
        Page<UserResponse> result = userService.getAll(role, status, search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Detail d'un utilisateur")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        UserResponse response = userService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier un utilisateur")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UUID adminId,
            HttpServletRequest httpRequest) {

        String role = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel = securityHelper.getCurrentChannel();
        String ip = securityHelper.getIpAddress(httpRequest);

        UserResponse response = userService.update(id, request, adminId, role, username, channel, ip);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspendre un utilisateur")
    public ResponseEntity<ApiResponse<UserResponse>> suspend(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID adminId,
            HttpServletRequest httpRequest) {

        String role = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel = securityHelper.getCurrentChannel();
        String ip = securityHelper.getIpAddress(httpRequest);

        UserResponse response = userService.suspend(id, adminId, role, username, channel, ip);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deverrouiller un utilisateur")
    public ResponseEntity<ApiResponse<UserResponse>> unlock(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID adminId,
            HttpServletRequest httpRequest) {

        String role = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel = securityHelper.getCurrentChannel();
        String ip = securityHelper.getIpAddress(httpRequest);

        UserResponse response = userService.unlock(id, adminId, role, username, channel, ip);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un utilisateur")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID adminId,
            HttpServletRequest httpRequest) {

        String role = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel = securityHelper.getCurrentChannel();
        String ip = securityHelper.getIpAddress(httpRequest);

        userService.delete(id, adminId, role, username, channel, ip);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private Pageable parsePageable(int page, int size, String sort) {
        java.util.Set<String> allowed = java.util.Set.of("createdAt", "username", "email", "role", "status");

        String property;
        Sort.Direction direction;

        if (sort == null || sort.isBlank()) {
            property = "createdAt";
            direction = Sort.Direction.DESC;
        } else {
            String[] parts = sort.split(",");
            property = allowed.contains(parts[0].trim()) ? parts[0].trim() : "createdAt";
            direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
        }

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
