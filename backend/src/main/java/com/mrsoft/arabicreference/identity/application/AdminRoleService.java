package com.mrsoft.arabicreference.identity.application;

import com.mrsoft.arabicreference.identity.application.StaffViews.PermissionView;
import com.mrsoft.arabicreference.identity.application.StaffViews.RoleView;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.DisplayNames;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.identity.domain.RoleCodes;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminPermissionEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminPermissionRepository;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminRoleEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminRoleRepository;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminRoleService {

    private static final Pattern CODE = Pattern.compile("^[A-Z][A-Z0-9_]{1,63}$");

    private final AdminRoleRepository roles;
    private final AdminPermissionRepository permissions;
    private final AuthorizationService authorization;
    private final AuditRecorder audit;
    private final StaffDirectory directory;
    private final TimeProvider timeProvider;

    public AdminRoleService(
            AdminRoleRepository roles,
            AdminPermissionRepository permissions,
            AuthorizationService authorization,
            AuditRecorder audit,
            StaffDirectory directory,
            TimeProvider timeProvider) {
        this.roles = roles;
        this.permissions = permissions;
        this.authorization = authorization;
        this.audit = audit;
        this.directory = directory;
        this.timeProvider = timeProvider;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.ROLE_VIEW + "')")
    public List<RoleView> list() {
        return roles.findAll().stream().map(directory::role).sorted((left, right) -> left.code().compareTo(right.code())).toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.ROLE_VIEW + "')")
    public RoleView get(UUID id) {
        return directory.role(roles.findById(id).orElseThrow(this::missing));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.PERMISSION_VIEW + "')")
    public List<PermissionView> permissions() {
        return permissions.findAll().stream()
                .map(permission -> new PermissionView(permission.getCode(), permission.getDescription()))
                .sorted((left, right) -> left.code().compareTo(right.code()))
                .toList();
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ROLE_CREATE + "')")
    public RoleView create(String code, String name, String description, List<String> permissionCodes) {
        String normalizedCode = normalizeCode(code);
        if (roles.existsByCode(normalizedCode)) {
            throw new ConflictException("That role code is already in use.");
        }
        Set<AdminPermissionEntity> granted = resolve(permissionCodes);
        authorization.requireGrantable(granted.stream().map(AdminPermissionEntity::getCode).toList());
        Instant now = timeProvider.now();
        AdminRoleEntity role = new AdminRoleEntity();
        role.setId(Ids.random());
        role.setCode(normalizedCode);
        role.setName(DisplayNames.normalize(name));
        role.setDescription(normalizeDescription(description));
        role.setSystemRole(false);
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        role.setPermissions(granted);
        roles.saveAndFlush(role);
        audit.record(authorization.requireAccess().userId(), AuditEventType.ADMIN_ROLE_CREATED, "admin_role", role.getId().toString(), Map.of("code", role.getCode()));
        return directory.role(role);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ROLE_EDIT + "')")
    public RoleView update(UUID id, String name, String description, List<String> permissionCodes, long version) {
        AdminRoleEntity role = roles.findById(id).orElseThrow(this::missing);
        if (role.getVersion() != version) {
            throw new ConflictException("The record was updated by someone else. Reload and try again.");
        }
        if (role.isSystemRole() && !authorization.requireAccess().permissions().containsAll(PermissionCatalog.all())) {
            throw new ForbiddenOperationException("Only a platform owner can edit a system role.");
        }
        if (name != null) {
            role.setName(DisplayNames.normalize(name));
        }
        if (description != null) {
            role.setDescription(normalizeDescription(description));
        }
        if (permissionCodes != null) {
            Set<AdminPermissionEntity> granted = resolve(permissionCodes);
            if (RoleCodes.PLATFORM_OWNER.equals(role.getCode())
                    && !granted.stream().map(AdminPermissionEntity::getCode).collect(java.util.stream.Collectors.toSet()).equals(Set.copyOf(PermissionCatalog.all()))) {
                throw new ForbiddenOperationException("The platform owner role must keep the full permission catalog.");
            }
            Set<String> current = role.getPermissions().stream().map(AdminPermissionEntity::getCode).collect(java.util.stream.Collectors.toSet());
            Set<String> incoming = granted.stream().map(AdminPermissionEntity::getCode).collect(java.util.stream.Collectors.toSet());
            Set<String> added = new HashSet<>(incoming);
            added.removeAll(current);
            authorization.requireGrantable(added);
            role.getPermissions().clear();
            role.getPermissions().addAll(granted);
        }
        role.setUpdatedAt(timeProvider.now());
        roles.saveAndFlush(role);
        audit.record(authorization.requireAccess().userId(), AuditEventType.ADMIN_ROLE_UPDATED, "admin_role", role.getId().toString(), Map.of("code", role.getCode()));
        return directory.role(role);
    }

    private Set<AdminPermissionEntity> resolve(List<String> permissionCodes) {
        Set<AdminPermissionEntity> granted = new HashSet<>();
        for (String code : permissionCodes == null ? List.<String>of() : permissionCodes) {
            if (!PermissionCatalog.exists(code)) {
                throw new ValidationException("Unknown permission.", List.of(new FieldErrorDetail("permissionCodes", "Unknown permission.")));
            }
            granted.add(permissions.findById(code).orElseThrow(() -> new ResourceNotFoundException("Permission was not found.")));
        }
        return granted;
    }

    private static String normalizeCode(String code) {
        if (code == null) {
            throw new ValidationException("Role code is invalid.", List.of(new FieldErrorDetail("code", "Use uppercase letters, digits, and underscores.")));
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        if (!CODE.matcher(normalized).matches()) {
            throw new ValidationException("Role code is invalid.", List.of(new FieldErrorDetail("code", "Use uppercase letters, digits, and underscores.")));
        }
        return normalized;
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank() || description.trim().length() > 500) {
            throw new ValidationException("Role description is invalid.", List.of(new FieldErrorDetail("description", "Use 1 to 500 characters.")));
        }
        return description.trim();
    }

    private ResourceNotFoundException missing() {
        return new ResourceNotFoundException("Admin role was not found.");
    }
}
