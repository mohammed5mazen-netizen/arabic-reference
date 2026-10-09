package com.mrsoft.arabicreference.admin.api;

import com.mrsoft.arabicreference.identity.application.AdminSessionService;
import com.mrsoft.arabicreference.identity.application.StaffViews.SessionView;
import com.mrsoft.arabicreference.identity.application.StaffViews.TokenPair;
import com.mrsoft.arabicreference.shared.web.ClientAddresses;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    private final AdminSessionService sessions;
    private final TimeProvider timeProvider;

    public AdminAuthController(AdminSessionService sessions, TimeProvider timeProvider) {
        this.sessions = sessions;
        this.timeProvider = timeProvider;
    }

    @PostMapping("/login")
    public ApiResponse<TokenPair> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return ApiResponses.ok(sessions.login(request.username(), request.password(), ClientAddresses.read(http)), timeProvider);
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenPair> refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest http) {
        return ApiResponses.ok(sessions.refresh(request.refreshToken(), ClientAddresses.read(http)), timeProvider);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        sessions.logout();
        return ApiResponses.ok(null, timeProvider);
    }

    @PostMapping("/change-password")
    public ApiResponse<TokenPair> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest http) {
        return ApiResponses.ok(sessions.changePassword(request.currentPassword(), request.newPassword(), ClientAddresses.read(http)), timeProvider);
    }

    @GetMapping("/session")
    public ApiResponse<SessionView> session() {
        return ApiResponses.ok(sessions.session(), timeProvider);
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank String newPassword) {
    }
}
