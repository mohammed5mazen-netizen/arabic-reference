package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LexicalSenseRepository extends JpaRepository<LexicalSenseEntity, UUID> {

    List<LexicalSenseEntity> findByLexicalEntryIdOrderByDisplayOrderAsc(UUID lexicalEntryId);

    @Query("select coalesce(max(sense.displayOrder), 0) from LexicalSenseEntity sense where sense.lexicalEntryId = :entryId")
    int maxOrder(@Param("entryId") UUID entryId);
}
