package com.mrsoft.arabicreference.identity.application;

import com.mrsoft.arabicreference.identity.application.StaffViews.AuditView;
import com.mrsoft.arabicreference.identity.application.StaffViews.DashboardView;
import com.mrsoft.arabicreference.identity.application.StaffViews.UserCounts;
import com.mrsoft.arabicreference.identity.domain.AccountStatus;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminAuditEventRepository;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {

    private final AdminUserRepository users;
    private final AdminAuditEventRepository events;
    private final AuthorizationService authorization;
    private final StaffDirectory directory;

    public AdminDashboardService(
            AdminUserRepository users,
            AdminAuditEventRepository events,
            AuthorizationService authorization,
            StaffDirectory directory) {
        this.users = users;
        this.events = events;
        this.authorization = authorization;
        this.directory = directory;
    }

    @Transactional(readOnly = true)
    public DashboardView dashboard() {
        authorization.requireAccess();
        UserCounts counts = authorization.has(PermissionCatalog.USER_VIEW)
                ? new UserCounts(users.count(), users.countByStatus(AccountStatus.ACTIVE), users.countByStatus(AccountStatus.LOCKED))
                : null;
        List<AuditView> recent = authorization.has(PermissionCatalog.AUDIT_VIEW)
                ? events.findAllByOrderByOccurredAtDesc(PageRequest.of(0, 8)).stream().map(directory::audit).toList()
                : null;
        return new DashboardView(counts, recent);
    }
}
