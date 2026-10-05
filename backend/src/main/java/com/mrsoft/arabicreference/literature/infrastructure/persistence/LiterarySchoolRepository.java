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

public interface LiterarySchoolRepository extends JpaRepository<LiterarySchoolEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select school from LiterarySchoolEntity school where school.id = :id")
    Optional<LiterarySchoolEntity> lockById(@Param("id") UUID id);

    Optional<LiterarySchoolEntity> findBySlug(String slug);

    Page<LiterarySchoolEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    List<LiterarySchoolEntity> findByStatusOrderByUpdatedAtDesc(PublicationStatus status);

    @Query("""
            select school from LiterarySchoolEntity school
            where school.publishedSnapshot is not null and school.status <> :archived
            order by school.nameNormalized asc
            """)
    List<LiterarySchoolEntity> visibleToPublic(@Param("archived") PublicationStatus archived);
}
