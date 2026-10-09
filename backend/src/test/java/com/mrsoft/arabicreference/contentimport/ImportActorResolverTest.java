package com.mrsoft.arabicreference.contentimport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.mrsoft.arabicreference.contentimport.application.ContentImportException;
import com.mrsoft.arabicreference.contentimport.application.ImportActorResolver;
import com.mrsoft.arabicreference.identity.domain.AccountStatus;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImportActorResolverTest {
    @Mock private AdminUserRepository users;

    @Test
    void requiresTheExistingDisabledSystemActor() {
        UUID actorId = UUID.randomUUID();
        AdminUserEntity actor = new AdminUserEntity();
        actor.setId(actorId);
        actor.setStatus(AccountStatus.DISABLED);
        when(users.findByUsername("content.importer")).thenReturn(Optional.of(actor));

        assertThat(new ImportActorResolver(users).requireDisabledActor()).isEqualTo(actorId);
    }

    @Test
    void rejectsMissingOrLoginEnabledImporterActor() {
        when(users.findByUsername("content.importer")).thenReturn(Optional.empty());
        ImportActorResolver resolver = new ImportActorResolver(users);
        assertThatThrownBy(resolver::requireDisabledActor)
                .isInstanceOf(ContentImportException.class)
                .hasMessageContaining("system actor is required");

        AdminUserEntity actor = new AdminUserEntity();
        actor.setStatus(AccountStatus.ACTIVE);
        when(users.findByUsername("content.importer")).thenReturn(Optional.of(actor));
        assertThatThrownBy(resolver::requireDisabledActor)
                .isInstanceOf(ContentImportException.class)
                .hasMessageContaining("must remain DISABLED");
    }
}
