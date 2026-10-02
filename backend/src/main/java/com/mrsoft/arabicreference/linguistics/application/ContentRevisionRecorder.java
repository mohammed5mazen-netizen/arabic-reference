package com.mrsoft.arabicreference.linguistics.application;

import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.ContentRevisionEntity;
import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.ContentRevisionRepository;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentRevisionRecorder {

    private final ContentRevisionRepository revisions;
    private final TimeProvider timeProvider;

    public ContentRevisionRecorder(ContentRevisionRepository revisions, TimeProvider timeProvider) {
        this.revisions = revisions;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public void record(String targetType, UUID targetId, Map<String, Object> snapshot, UUID actorId, String reason) {
        ContentRevisionEntity revision = new ContentRevisionEntity();
        revision.setId(Ids.random());
        revision.setTargetType(targetType);
        revision.setTargetId(targetId);
        revision.setRevisionNumber(revisions.maxNumber(targetType, targetId) + 1);
        revision.setSnapshot(snapshot);
        revision.setActorId(actorId);
        revision.setChangeReason(reason);
        revision.setCreatedAt(timeProvider.now());
        revisions.save(revision);
    }

    @Transactional(readOnly = true)
    public java.util.List<ContentRevisionEntity> history(String targetType, UUID targetId) {
        return revisions.findByTargetTypeAndTargetIdOrderByRevisionNumberDesc(targetType, targetId);
    }
}
