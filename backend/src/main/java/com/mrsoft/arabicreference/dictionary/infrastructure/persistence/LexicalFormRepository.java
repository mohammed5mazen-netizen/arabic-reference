package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import com.mrsoft.arabicreference.dictionary.domain.FormType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LexicalFormRepository extends JpaRepository<LexicalFormEntity, UUID> {

    List<LexicalFormEntity> findByLexicalEntryIdOrderByDisplayOrderAsc(UUID lexicalEntryId);

    Optional<LexicalFormEntity> findFirstByLexicalEntryIdAndFormTypeAndNormalizedForm(
            UUID lexicalEntryId, FormType formType, String normalizedForm);

    @Query("""
            select form from LexicalFormEntity form
            where form.normalizedForm = :normalized
              and form.status = com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus.PUBLISHED
            """)
    List<LexicalFormEntity> findPublishedByNormalized(@Param("normalized") String normalized);

    @Query("select coalesce(max(form.displayOrder), 0) from LexicalFormEntity form where form.lexicalEntryId = :entryId")
    int maxOrder(@Param("entryId") UUID entryId);
}
