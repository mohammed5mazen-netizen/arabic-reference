package com.mrsoft.arabicreference.identity.application;

import com.mrsoft.arabicreference.identity.domain.AccountStatus;
import com.mrsoft.arabicreference.identity.domain.DisplayNames;
import com.mrsoft.arabicreference.identity.domain.Emails;
import com.mrsoft.arabicreference.identity.domain.PasswordPolicy;
import com.mrsoft.arabicreference.identity.domain.RoleCodes;
import com.mrsoft.arabicreference.identity.domain.Usernames;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminRoleRepository;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRepository;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRoleEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRoleKey;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRoleRepository;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OwnerBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OwnerBootstrap.class);

    private final AdminUserRepository users;
    private final AdminRoleRepository roles;
    private final AdminUserRoleRepository links;
    private final PasswordEncoder passwordEncoder;
    private final AdminSecurityProperties properties;
    private final TimeProvider timeProvider;
    private final JdbcTemplate jdbc;
    private final ObjectProvider<OwnerBootstrap> self;

    public OwnerBootstrap(
            AdminUserRepository users,
            AdminRoleRepository roles,
            AdminUserRoleRepository links,
            PasswordEncoder passwordEncoder,
            AdminSecurityProperties properties,
            TimeProvider timeProvider,
            JdbcTemplate jdbc,
            ObjectProvider<OwnerBootstrap> self) {
        this.users = users;
        this.roles = roles;
        this.links = links;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.timeProvider = timeProvider;
        this.jdbc = jdbc;
        this.self = self;
    }

    @Override
    public void run(ApplicationArguments args) {
        self.getObject().ensure();
    }

    @Transactional
    public void ensure() {
        jdbc.queryForList("SELECT pg_advisory_xact_lock(48291017)");
        if (users.countPlatformOwners() > 0) {
            return;
        }
        AdminSecurityProperties.Bootstrap bootstrap = properties.getBootstrap();
        if (!bootstrap.complete()) {
            log.info("Platform owner bootstrap skipped because BOOTSTRAP_OWNER_* is incomplete.");
            return;
        }
        String username = Usernames.normalize(bootstrap.getUsername());
        String email = Emails.normalize(bootstrap.getEmail());
        String displayName = DisplayNames.normalize(bootstrap.getDisplayName());
        PasswordPolicy.check(bootstrap.getPassword());
        if (users.existsByUsername(username) || users.existsByEmail(email)) {
            throw new IllegalStateException("Platform owner bootstrap conflicts with an existing account.");
        }
        Instant now = timeProvider.now();
        AdminUserEntity owner = new AdminUserEntity();
        owner.setId(Ids.random());
        owner.setUsername(username);
        owner.setEmail(email);
        owner.setDisplayName(displayName);
        owner.setPasswordHash(passwordEncoder.encode(bootstrap.getPassword()));
        owner.setStatus(AccountStatus.ACTIVE);
        owner.setMustChangePassword(false);
        owner.setFailedLoginAttempts(0);
        owner.setPasswordChangedAt(now);
        owner.setCreatedAt(now);
        owner.setUpdatedAt(now);
        users.saveAndFlush(owner);
        var role = roles.findByCode(RoleCodes.PLATFORM_OWNER).orElseThrow();
        AdminUserRoleEntity link = new AdminUserRoleEntity();
        link.setId(new AdminUserRoleKey(owner.getId(), role.getId()));
        link.setAssignedAt(now);
        links.saveAndFlush(link);
        log.info("Bootstrapped platform owner username={}", username);
    }
}
