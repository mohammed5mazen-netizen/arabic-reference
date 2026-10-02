package com.mrsoft.arabicreference.identity.application;

import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminAuditEventEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminAuditEventRepository;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.kernel.trace.TraceIds;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditRecorder {

    private final AdminAuditEventRepository events;
    private final TimeProvider timeProvider;

    public AuditRecorder(AdminAuditEventRepository events, TimeProvider timeProvider) {
        this.events = events;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public void record(UUID actorId, AuditEventType type, String targetType, String targetId, Map<String, String> metadata) {
        AdminAuditEventEntity event = new AdminAuditEventEntity();
        event.setId(Ids.random());
        event.setActorId(actorId);
        event.setEventType(type);
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setOccurredAt(timeProvider.now());
        event.setTraceId(TraceIds.current());
        event.setMetadata(safe(metadata));
        events.save(event);
    }

    static Map<String, String> safe(Map<String, String> metadata) {
        Map<String, String> copy = new LinkedHashMap<>();
        if (metadata != null) {
            metadata.forEach((key, value) -> {
                String lowered = key == null ? "" : key.toLowerCase(Locale.ROOT);
                if (lowered.contains("password") || lowered.contains("token") || lowered.contains("secret")) {
                    return;
                }
                copy.put(key, value);
            });
        }
        return Map.copyOf(copy);
    }
}
