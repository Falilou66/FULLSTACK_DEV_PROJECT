package sn.samabank.audit.shared;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class SecurityContextHelper {

    public String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "UNKNOWN";
        return auth.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .orElse("UNKNOWN");
    }

    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UUID)) return null;
        return (UUID) auth.getPrincipal();
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "UNKNOWN";

        Object details = auth.getDetails();

        if (details instanceof String username && !username.isBlank()) {
            return username;
        }

        if (details instanceof Map<?, ?> map) {
            Object username = map.get("username");
            if (username instanceof String s && !s.isBlank()) {
                return s;
            }
        }

        return "UNKNOWN";
    }

    public String deriveChannel(String role) {
        return switch (role) {
            case "ADMIN", "TELLER" -> "BACKOFFICE";
            case "CUSTOMER"        -> "WEB";
            default                -> "API";
        };
    }

    public String getCurrentChannel() {
        return deriveChannel(getCurrentRole());
    }

    public String getIpAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
