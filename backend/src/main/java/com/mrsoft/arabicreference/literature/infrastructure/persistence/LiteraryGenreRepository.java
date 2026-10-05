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

public interface LiteraryGenreRepository extends JpaRepository<LiteraryGenreEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select genre from LiteraryGenreEntity genre where genre.id = :id")
    Optional<LiteraryGenreEntity> lockById(@Param("id") UUID id);

    Optional<LiteraryGenreEntity> findBySlug(String slug);

    Page<LiteraryGenreEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    List<LiteraryGenreEntity> findByStatusOrderByUpdatedAtDesc(PublicationStatus status);

    @Query("""
            select genre from LiteraryGenreEntity genre
            where genre.publishedSnapshot is not null and genre.status <> :archived
            order by genre.nameNormalized asc
            """)
    List<LiteraryGenreEntity> visibleToPublic(@Param("archived") PublicationStatus archived);
}
