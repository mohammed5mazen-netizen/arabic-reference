package com.mrsoft.arabicreference.identity.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminRefreshTokenRepository extends JpaRepository<AdminRefreshTokenEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from AdminRefreshTokenEntity token where token.tokenHash = :hash")
    Optional<AdminRefreshTokenEntity> lockByHash(@Param("hash") String hash);

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query("""
            update AdminRefreshTokenEntity token
               set token.revokedAt = :now
             where token.familyId = :familyId
               and token.revokedAt is null
            """)
    int revokeFamily(@Param("familyId") UUID familyId, @Param("now") Instant now);

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query("""
            update AdminRefreshTokenEntity token
               set token.revokedAt = :now
             where token.userId = :userId
               and token.revokedAt is null
            """)
    int revokeAllForUser(@Param("userId") UUID userId, @Param("now") Instant now);
}
