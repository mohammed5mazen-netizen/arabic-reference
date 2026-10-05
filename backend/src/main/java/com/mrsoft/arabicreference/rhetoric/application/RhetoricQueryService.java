package com.mrsoft.arabicreference.rhetoric.application;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.PublicDevice;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.PublicLink;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricViews.PublicTopic;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricDeviceRepository;
import com.mrsoft.arabicreference.rhetoric.infrastructure.persistence.RhetoricTopicRepository;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RhetoricQueryService {
    private final RhetoricTopicRepository topics;
    private final RhetoricDeviceRepository devices;

    public RhetoricQueryService(RhetoricTopicRepository topics, RhetoricDeviceRepository devices) {
        this.topics = topics;
        this.devices = devices;
    }

    @Transactional(readOnly = true)
    public List<PublicLink> topics() {
        List<PublicLink> links = new ArrayList<>();
        for (var topic : topics.visibleToPublic(PublicationStatus.ARCHIVED)) {
            Map<String, Object> snapshot = topic.getPublishedSnapshot();
            links.add(new PublicLink(text(snapshot.get("title")), text(snapshot.get("slug")), text(snapshot.get("summary"))));
        }
        return links;
    }

    @Transactional(readOnly = true)
    public PublicTopic topic(String slug) {
        var topic = topics.findBySlug(slug).filter(item -> item.visibleToPublic()).orElseThrow(() -> new ResourceNotFoundException("Topic was not found."));
        Map<String, Object> snapshot = topic.getPublishedSnapshot();
        List<PublicLink> deviceLinks = new ArrayList<>();
        for (var device : devices.visibleToPublic(PublicationStatus.ARCHIVED)) {
            Map<String, Object> deviceSnapshot = device.getPublishedSnapshot();
            if (topic.getId().toString().equals(text(deviceSnapshot.get("topicId")))) {
                deviceLinks.add(new PublicLink(text(deviceSnapshot.get("name")), text(deviceSnapshot.get("slug")), text(deviceSnapshot.get("shortDefinition"))));
            }
        }
        return new PublicTopic(text(snapshot.get("title")), text(snapshot.get("slug")), text(snapshot.get("summary")), text(snapshot.get("category")), text(snapshot.get("categoryLabel")), deviceLinks, maps(snapshot.get("sources")));
    }

    @Transactional(readOnly = true)
    public PublicDevice device(String slug) {
        var device = devices.findBySlug(slug).filter(item -> item.visibleToPublic()).orElseThrow(() -> new ResourceNotFoundException("Device was not found."));
        Map<String, Object> snapshot = device.getPublishedSnapshot();
        return new PublicDevice(text(snapshot.get("name")), text(snapshot.get("slug")), text(snapshot.get("shortDefinition")), text(snapshot.get("detailedExplanation")), map(snapshot.get("topic")), maps(snapshot.get("components")), maps(snapshot.get("examples")), maps(snapshot.get("relations")), maps(snapshot.get("sources")));
    }

    private static String text(Object value) { return value == null ? "" : String.valueOf(value); }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) { return value instanceof Map<?, ?> raw ? (Map<String, Object>) raw : Map.of(); }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> items)) return List.of();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : items) if (item instanceof Map<?, ?> raw) result.add((Map<String, Object>) raw);
        return result;
    }
}
