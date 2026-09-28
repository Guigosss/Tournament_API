package com.technofuturtic.tournament_api.dl.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tournament")
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false) @ToString
public class TournamentEntity extends BaseEntity {

    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @Column(nullable = false)
    private String name;

    @Getter @Setter
    @Column(nullable = false)
    private String description;

    @Getter @Setter
    @Column(nullable = false)
    private Integer maxParticipants;

    @Getter @Setter
    @Column(nullable = false)
    private String format; //- Enum

    @Getter @Setter
    @Column(nullable = false)
    private String status; //- Enum

    @Getter @Setter
    @Column(nullable = false)
    private LocalDate startDate;

    @Getter @Setter
    @Column(nullable = false)
    private LocalDate registrationStartDate;

    @Getter @Setter
    @Column(nullable = false)
    private LocalDate registrationEndDate;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private UserEntity organizer;
}
