package com.mrsoft.arabicreference.contentimport.application;

import com.mrsoft.arabicreference.identity.domain.AccountStatus;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ImportActorResolver {
    private static final String IMPORT_ACTOR_USERNAME = "content.importer";

    private final AdminUserRepository users;

    public ImportActorResolver(AdminUserRepository users) {
        this.users = users;
    }

    public UUID requireDisabledActor() {
        var actor = users.findByUsername(IMPORT_ACTOR_USERNAME)
                .orElseThrow(() -> new ContentImportException("The disabled content.importer system actor is required."));
        if (actor.getStatus() != AccountStatus.DISABLED) {
            throw new ContentImportException("The content.importer system actor must remain DISABLED.");
        }
        return actor.getId();
    }
}
