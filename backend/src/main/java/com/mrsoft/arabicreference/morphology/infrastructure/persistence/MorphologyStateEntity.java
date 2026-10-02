package com.mrsoft.arabicreference.morphology.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "morphology_state")
public class MorphologyStateEntity {

    @Id
    private short id;

    @Column(nullable = false)
    private long generation;

    public short getId() {
        return id;
    }

    public long getGeneration() {
        return generation;
    }

    public void setGeneration(long generation) {
        this.generation = generation;
    }
}
