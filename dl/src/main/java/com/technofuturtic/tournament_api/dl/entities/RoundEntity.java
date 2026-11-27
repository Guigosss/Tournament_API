package com.technofuturtic.tournament_api.dl.entities;

import com.technofuturtic.tournament_api.dl.enums.RoundType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "round")
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false) @ToString
public class RoundEntity {

    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoundType type;

    @Getter @Setter
    @Column(nullable = false)
    private Integer orderIndex;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id", nullable = false)
    private PhaseEntity phase;
}
