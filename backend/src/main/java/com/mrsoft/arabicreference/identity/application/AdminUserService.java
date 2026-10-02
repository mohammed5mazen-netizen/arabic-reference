package com.mrsoft.arabicreference.identity.application;

import com.mrsoft.arabicreference.identity.application.StaffViews.AdminUserView;
import com.mrsoft.arabicreference.identity.application.StaffViews.CreatedAdminUser;
import com.mrsoft.arabicreference.identity.application.StaffViews.PageResult;
import com.mrsoft.arabicreference.identity.application.StaffViews.ResetPasswordResult;
import com.mrsoft.arabicreference.identity.domain.AccountStatus;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.DisplayNames;
import com.mrsoft.arabicreference.identity.domain.Emails;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.identity.domain.RoleCodes;
import com.mrsoft.arabicreference.identity.domain.TemporaryPasswords;
import com.mrsoft.arabicreference.identity.domain.Usernames;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminRoleEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminRoleRepository;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRepository;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRoleEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRoleKey;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRoleRepository;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final AdminUserRepository users;
    private final AdminRoleRepository roles;
    private final AdminUserRoleRepository links;
    private final PasswordEncoder passwordEncoder;
    private final AuditRecorder audit;
    private final AuthorizationService authorization;
    private final StaffDirectory directory;
    private final TimeProvider timeProvider;
    private final AdminSessionService sessions;

    public AdminUserService(
            AdminUserRepository users,
            AdminRoleRepository roles,
            AdminUserRoleRepository links,
            PasswordEncoder passwordEncoder,
            AuditRecorder audit,
            AuthorizationService authorization,
            StaffDirectory directory,
            TimeProvider timeProvider,
            AdminSessionService sessions) {
        this.users = users;
        this.roles = roles;
        this.links = links;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
        this.authorization = authorization;
        this.directory = directory;
        this.timeProvider = timeProvider;
        this.sessions = sessions;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.USER_VIEW + "')")
    public PageResult<AdminUserView> list(int page, int size) {
        int bounded = bound(page, size);
        var result = users.findAll(PageRequest.of(page, bounded, Sort.by("username")));
        return new PageResult<>(result.stream().map(directory::user).toList(), page, bounded, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.USER_VIEW + "')")
    public AdminUserView get(UUID id) {
        return directory.user(users.findById(id).orElseThrow(this::missing));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.USER_CREATE + "')")
    public CreatedAdminUser create(String username, String email, String displayName, List<UUID> roleIds) {
        String normalizedUsername = Usernames.normalize(username);
        String normalizedEmail = Emails.normalize(email);
        String normalizedName = DisplayNames.normalize(displayName);
        if (users.existsByUsername(normalizedUsername) || users.existsByEmail(normalizedEmail)) {
            throw new ConflictException("The username or email is already in use.");
        }
        Instant now = timeProvider.now();
        String temporaryPassword = TemporaryPasswords.generate();
        AdminUserEntity user = new AdminUserEntity();
        user.setId(Ids.random());
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setDisplayName(normalizedName);
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setStatus(AccountStatus.ACTIVE);
        user.setMustChangePassword(true);
        user.setFailedLoginAttempts(0);
        user.setPasswordChangedAt(now);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        users.saveAndFlush(user);
        replaceRoles(user, roleIds == null ? List.of() : roleIds, false);
        audit.record(authorization.requireAccess().userId(), AuditEventType.ADMIN_USER_CREATED, "admin_user", user.getId().toString(), Map.of("username", user.getUsername()));
        return new CreatedAdminUser(directory.user(user), temporaryPassword);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.USER_EDIT + "')")
    public AdminUserView update(UUID id, String email, String displayName, long version) {
        AdminUserEntity user = locked(id, version);
        if (email != null) {
            String normalized = Emails.normalize(email);
            if (!normalized.equals(user.getEmail()) && users.existsByEmail(normalized)) {
                throw new ConflictException("The username or email is already in use.");
            }
            user.setEmail(normalized);
        }
        if (displayName != null) {
            user.setDisplayName(DisplayNames.normalize(displayName));
        }
        user.setUpdatedAt(timeProvider.now());
        users.saveAndFlush(user);
        audit.record(authorization.requireAccess().userId(), AuditEventType.ADMIN_USER_UPDATED, "admin_user", user.getId().toString(), Map.of("username", user.getUsername()));
        return directory.user(user);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.USER_ACTIVATE + "')")
    public AdminUserView activate(UUID id, long version) {
        AdminUserEntity user = locked(id, version);
        user.setStatus(AccountStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setUpdatedAt(timeProvider.now());
        users.saveAndFlush(user);
        audit.record(authorization.requireAccess().userId(), AuditEventType.ADMIN_USER_ACTIVATED, "admin_user", id.toString(), Map.of("username", user.getUsername()));
        return directory.user(user);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.USER_DEACTIVATE + "')")
    public AdminUserView deactivate(UUID id, long version) {
        AdminUserEntity user = locked(id, version);
        protectLastOwner(user);
        user.setStatus(AccountStatus.DISABLED);
        user.setUpdatedAt(timeProvider.now());
        users.saveAndFlush(user);
        sessions.revokeRefreshTokens(id);
        audit.record(authorization.requireAccess().userId(), AuditEventType.ADMIN_USER_DEACTIVATED, "admin_user", id.toString(), Map.of("username", user.getUsername()));
        return directory.user(user);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.USER_UNLOCK + "')")
    public AdminUserView unlock(UUID id, long version) {
        AdminUserEntity user = locked(id, version);
        if (user.getStatus() != AccountStatus.LOCKED) {
            throw new ConflictException("Only a locked account can be unlocked.");
        }
        user.setStatus(AccountStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setUpdatedAt(timeProvider.now());
        users.saveAndFlush(user);
        audit.record(authorization.requireAccess().userId(), AuditEventType.ADMIN_USER_UNLOCKED, "admin_user", id.toString(), Map.of("username", user.getUsername()));
        return directory.user(user);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.USER_RESET_PASSWORD + "')")
    public ResetPasswordResult resetPassword(UUID id, long version) {
        AdminUserEntity user = locked(id, version);
        String temporaryPassword = TemporaryPasswords.generate();
        Instant now = timeProvider.now();
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        user.setPasswordChangedAt(now);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        if (user.getStatus() == AccountStatus.LOCKED) {
            user.setStatus(AccountStatus.ACTIVE);
        }
        user.setUpdatedAt(now);
        users.saveAndFlush(user);
        sessions.revokeRefreshTokens(id);
        audit.record(authorization.requireAccess().userId(), AuditEventType.ADMIN_PASSWORD_RESET, "admin_user", id.toString(), Map.of("username", user.getUsername()));
        return new ResetPasswordResult(directory.user(user), temporaryPassword);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ROLE_ASSIGN + "')")
    public AdminUserView replaceRoles(UUID id, List<UUID> roleIds, long version) {
        AdminUserEntity user = locked(id, version);
        Set<String> previous = Set.copyOf(links.findRoleCodes(id));
        replaceRoles(user, roleIds, true);
        user.setUpdatedAt(timeProvider.now());
        users.saveAndFlush(user);
        Set<String> current = Set.copyOf(links.findRoleCodes(id));
        UUID actorId = authorization.requireAccess().userId();
        previous.stream().filter(code -> !current.contains(code)).forEach(code ->
                audit.record(actorId, AuditEventType.ADMIN_ROLE_REMOVED, "admin_user", id.toString(), Map.of("username", user.getUsername(), "role", code)));
        current.stream().filter(code -> !previous.contains(code)).forEach(code ->
                audit.record(actorId, AuditEventType.ADMIN_ROLE_ASSIGNED, "admin_user", id.toString(), Map.of("username", user.getUsername(), "role", code)));
        return directory.user(user);
    }

    private void replaceRoles(AdminUserEntity user, List<UUID> roleIds, boolean guardOwner) {
        List<AdminRoleEntity> selected = resolveRoles(roleIds);
        if (guardOwner) {
            boolean keepsOwner = selected.stream().anyMatch(role -> RoleCodes.PLATFORM_OWNER.equals(role.getCode()));
            boolean isOwner = links.countRole(user.getId(), RoleCodes.PLATFORM_OWNER) > 0;
            if (isOwner && !keepsOwner) {
                protectLastOwner(user);
            }
        }
        links.deleteForUser(user.getId());
        Instant now = timeProvider.now();
        for (AdminRoleEntity role : selected) {
            if (links.countAssignment(user.getId(), role.getId()) > 0) {
                continue;
            }
            AdminUserRoleEntity link = new AdminUserRoleEntity();
            link.setId(new AdminUserRoleKey(user.getId(), role.getId()));
            link.setAssignedAt(now);
            links.save(link);
        }
        links.flush();
    }

    private List<AdminRoleEntity> resolveRoles(List<UUID> roleIds) {
        Set<UUID> distinct = new LinkedHashSet<>(roleIds == null ? List.of() : roleIds);
        List<AdminRoleEntity> selected = new ArrayList<>();
        for (UUID roleId : distinct) {
            AdminRoleEntity role = roles.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Admin role was not found."));
            authorization.requireGrantable(role.getPermissions().stream().map(permission -> permission.getCode()).toList());
            selected.add(role);
        }
        return selected;
    }

    private AdminUserEntity locked(UUID id, long version) {
        roles.lockPlatformOwnerRole();
        AdminUserEntity user = users.lockById(id).orElseThrow(this::missing);
        if (user.getVersion() != version) {
            throw new ConflictException("The record was updated by someone else. Reload and try again.");
        }
        return user;
    }

    private void protectLastOwner(AdminUserEntity user) {
        boolean owner = links.countRole(user.getId(), RoleCodes.PLATFORM_OWNER) > 0;
        if (owner && user.getStatus() == AccountStatus.ACTIVE && users.countOtherActiveOwners(user.getId()) == 0) {
            throw new ForbiddenOperationException("The last active platform owner cannot be deactivated or removed.");
        }
    }

    private ResourceNotFoundException missing() {
        return new ResourceNotFoundException("Admin user was not found.");
    }

    private static int bound(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ValidationException(
                    "Page request is invalid.",
                    List.of(new FieldErrorDetail("page", "Page must be zero or greater and size must be from 1 to 100.")));
        }
        return size;
    }
}
