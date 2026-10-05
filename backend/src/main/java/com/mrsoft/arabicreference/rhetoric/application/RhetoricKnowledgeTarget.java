package com.mrsoft.arabicreference.rhetoric.application;

import com.mrsoft.arabicreference.content.domain.KnowledgeTargetSource;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetType;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricDeviceRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RhetoricKnowledgeTarget implements KnowledgeTargetSource {
    private final RhetoricDeviceRepository devices;

    public RhetoricKnowledgeTarget(RhetoricDeviceRepository devices) {
        this.devices = devices;
    }

    @Override
    public boolean supports(KnowledgeTargetType type) {
        return type == KnowledgeTargetType.RHETORIC_DEVICE;
    }

    @Override
    public Optional<KnowledgeTarget> published(UUID id) {
        return devices.findById(id).filter(device -> device.visibleToPublic()).map(device -> new KnowledgeTarget(
                KnowledgeTargetType.RHETORIC_DEVICE,
                id,
                String.valueOf(device.getPublishedSnapshot().get("name")),
                "/rhetoric/devices/" + device.getPublishedSnapshot().get("slug")));
    }
}
