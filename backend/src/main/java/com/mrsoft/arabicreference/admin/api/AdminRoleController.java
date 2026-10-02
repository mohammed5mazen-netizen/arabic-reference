package com.mrsoft.arabicreference.admin.api;

import com.mrsoft.arabicreference.identity.application.AdminRoleService;
import com.mrsoft.arabicreference.identity.application.StaffViews.PermissionView;
import com.mrsoft.arabicreference.identity.application.StaffViews.RoleView;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminRoleController {

    private final AdminRoleService roles;
    private final TimeProvider timeProvider;

    public AdminRoleController(AdminRoleService roles, TimeProvider timeProvider) {
        this.roles = roles;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleView>> list() {
        return ApiResponses.ok(roles.list(), timeProvider);
    }

    @PostMapping("/roles")
    public ApiResponse<RoleView> create(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponses.ok(roles.create(request.code(), request.name(), request.description(), request.permissionCodes()), timeProvider);
    }

    @GetMapping("/roles/{id}")
    public ApiResponse<RoleView> get(@PathVariable UUID id) {
        return ApiResponses.ok(roles.get(id), timeProvider);
    }

    @PatchMapping("/roles/{id}")
    public ApiResponse<RoleView> update(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponses.ok(
                roles.update(id, request.name(), request.description(), request.permissionCodes(), request.version()),
                timeProvider);
    }

    @GetMapping("/permissions")
    public ApiResponse<List<PermissionView>> permissions() {
        return ApiResponses.ok(roles.permissions(), timeProvider);
    }

    public record CreateRoleRequest(
            @NotBlank String code,
            @NotBlank String name,
            @NotBlank String description,
            List<String> permissionCodes) {
    }

    public record UpdateRoleRequest(String name, String description, List<String> permissionCodes, @NotNull Long version) {
    }
}
