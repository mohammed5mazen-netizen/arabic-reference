package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "literary_work_alias")
public class LiteraryWorkAliasEntity {

    @Id
    private UUID id;

    @Column(name = "work_id", nullable = false)
    private UUID workId;

    @Column(nullable = false, length = 200)
    private String alias;

    @Column(nullable = false, length = 200)
    private String normalized;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getWorkId() { return workId; }
    public void setWorkId(UUID workId) { this.workId = workId; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public String getNormalized() { return normalized; }
    public void setNormalized(String normalized) { this.normalized = normalized; }
}
