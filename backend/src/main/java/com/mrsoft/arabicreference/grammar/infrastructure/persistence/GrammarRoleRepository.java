package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarRoleRepository extends JpaRepository<GrammarRoleEntity, UUID> {
    Optional<GrammarRoleEntity> findByCode(String code);
    List<GrammarRoleEntity> findAllByOrderByLabelArAsc();
}
