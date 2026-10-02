package com.mrsoft.arabicreference.admin.api;

import com.mrsoft.arabicreference.identity.application.AdminUserService;
import com.mrsoft.arabicreference.identity.application.StaffViews.AdminUserView;
import com.mrsoft.arabicreference.identity.application.StaffViews.CreatedAdminUser;
import com.mrsoft.arabicreference.identity.application.StaffViews.PageResult;
import com.mrsoft.arabicreference.identity.application.StaffViews.ResetPasswordResult;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService users;
    private final TimeProvider timeProvider;

    public AdminUserController(AdminUserService users, TimeProvider timeProvider) {
        this.users = users;
        this.timeProvider = timeProvider;
    }

    @GetMapping
    public ApiResponse<PageResult<AdminUserView>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(users.list(page, size), timeProvider);
    }

    @PostMapping
    public ApiResponse<CreatedAdminUser> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponses.ok(
                users.create(request.username(), request.email(), request.displayName(), request.roleIds()),
                timeProvider);
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminUserView> get(@PathVariable UUID id) {
        return ApiResponses.ok(users.get(id), timeProvider);
    }

    @PatchMapping("/{id}")
    public ApiResponse<AdminUserView> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponses.ok(users.update(id, request.email(), request.displayName(), request.version()), timeProvider);
    }

    @PostMapping("/{id}/activate")
    public ApiResponse<AdminUserView> activate(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) {
        return ApiResponses.ok(users.activate(id, request.version()), timeProvider);
    }

    @PostMapping("/{id}/deactivate")
    public ApiResponse<AdminUserView> deactivate(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) {
        return ApiResponses.ok(users.deactivate(id, request.version()), timeProvider);
    }

    @PostMapping("/{id}/unlock")
    public ApiResponse<AdminUserView> unlock(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) {
        return ApiResponses.ok(users.unlock(id, request.version()), timeProvider);
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<ResetPasswordResult> resetPassword(@PathVariable UUID id, @Valid @RequestBody VersionRequest request) {
        return ApiResponses.ok(users.resetPassword(id, request.version()), timeProvider);
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<AdminUserView> replaceRoles(@PathVariable UUID id, @Valid @RequestBody ReplaceRolesRequest request) {
        return ApiResponses.ok(users.replaceRoles(id, request.roleIds(), request.version()), timeProvider);
    }

    public record CreateUserRequest(
            @NotBlank String username,
            @NotBlank String email,
            @NotBlank String displayName,
            List<UUID> roleIds) {
    }

    public record UpdateUserRequest(String email, String displayName, @NotNull Long version) {
    }

    public record VersionRequest(@NotNull Long version) {
    }

    public record ReplaceRolesRequest(@NotNull List<UUID> roleIds, @NotNull Long version) {
    }
}
