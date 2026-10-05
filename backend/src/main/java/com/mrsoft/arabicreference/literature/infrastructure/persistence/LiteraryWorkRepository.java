package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LiteraryWorkRepository extends JpaRepository<LiteraryWorkEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select work from LiteraryWorkEntity work where work.id = :id")
    Optional<LiteraryWorkEntity> lockById(@Param("id") UUID id);

    Optional<LiteraryWorkEntity> findBySlug(String slug);

    Page<LiteraryWorkEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    List<LiteraryWorkEntity> findByStatusOrderByUpdatedAtDesc(PublicationStatus status);

    @Query("""
            select work from LiteraryWorkEntity work
            where work.publishedSnapshot is not null and work.status <> :archived
            """)
    List<LiteraryWorkEntity> visibleToPublic(@Param("archived") PublicationStatus archived);
}
