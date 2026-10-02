package com.mrsoft.arabicreference.identity.application;

import com.mrsoft.arabicreference.identity.application.StaffViews.AuditView;
import com.mrsoft.arabicreference.identity.application.StaffViews.PageResult;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminAuditEventRepository;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuditService {

    private final AdminAuditEventRepository events;
    private final StaffDirectory directory;

    public AdminAuditService(AdminAuditEventRepository events, StaffDirectory directory) {
        this.events = events;
        this.directory = directory;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.AUDIT_VIEW + "')")
    public PageResult<AuditView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ValidationException(
                    "Page request is invalid.",
                    List.of(new FieldErrorDetail("page", "Page must be zero or greater and size must be from 1 to 100.")));
        }
        var result = events.findAllByOrderByOccurredAtDesc(PageRequest.of(page, size));
        return new PageResult<>(result.stream().map(directory::audit).toList(), page, size, result.getTotalElements());
    }
}
