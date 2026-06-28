package sn.samabank.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.samabank.auth.dto.*;
import sn.samabank.auth.service.AuthService;
import sn.samabank.auth.shared.ApiResponse;
import sn.samabank.auth.shared.SecurityContextHelper;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentification et gestion des tokens JWT")
public class AuthController {

    private static final String CHANNEL_HEADER = "X-Channel";

    private final AuthService authService;
    private final SecurityContextHelper securityHelper;

    public AuthController(AuthService authService, SecurityContextHelper securityHelper) {
        this.authService = authService;
        this.securityHelper = securityHelper;
    }

    @PostMapping("/login")
    @Operation(summary = "Authentification")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ip = securityHelper.getIpAddress(httpRequest);
        String channel = resolveChannel(httpRequest);
        LoginResponse response = authService.login(request, ip, channel);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renouveler le token")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest) {

        String ip = securityHelper.getIpAddress(httpRequest);
        String channel = resolveChannel(httpRequest);
        LoginResponse response = authService.refresh(request, ip, channel);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Deconnexion")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UUID userId,
            HttpServletRequest httpRequest) {

        String ip = securityHelper.getIpAddress(httpRequest);
        String channel = resolveChannel(httpRequest);
        authService.logout(userId, ip, channel);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Changer son mot de passe")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {

        String ip = securityHelper.getIpAddress(httpRequest);
        String channel = securityHelper.getCurrentChannel();

        authService.changePassword(userId, request, ip, channel);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Demander la reinitialisation du mot de passe")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {

        String ip = securityHelper.getIpAddress(httpRequest);
        authService.forgotPassword(request, ip, "API");
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reinitialiser le mot de passe avec un token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {

        String ip = securityHelper.getIpAddress(httpRequest);
        authService.resetPassword(request, ip, "API");
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/me")
    @Operation(summary = "Profil utilisateur courant")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(
            @AuthenticationPrincipal UUID userId,
            HttpServletRequest request) {

        String correlationId = request.getHeader("X-Correlation-Id");
        Map<String, Object> profile = Map.of(
                "userId", userId.toString(),
                "message", "Profil charge depuis le JWT"
        );
        return ResponseEntity.ok(ApiResponse.ok(profile, correlationId));
    }

    private String resolveChannel(HttpServletRequest request) {
        String headerChannel = request.getHeader(CHANNEL_HEADER);
        if (headerChannel != null && !headerChannel.isBlank()) {
            return headerChannel.toUpperCase();
        }
        String role = securityHelper.getCurrentRole();
        if (!"UNKNOWN".equals(role)) {
            return securityHelper.deriveChannel(role);
        }
        return "API";
    }
}
