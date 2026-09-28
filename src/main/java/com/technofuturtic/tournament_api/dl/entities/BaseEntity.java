package com.technofuturtic.tournament_api.dl.entities;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode @ToString
public abstract class BaseEntity {

    @Getter @Setter
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Getter @Setter
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
