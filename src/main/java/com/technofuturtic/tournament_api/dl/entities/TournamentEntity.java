package com.technofuturtic.tournament_api.dl.entities;

import com.technofuturtic.tournament_api.dl.enums.ParticipantType;
import com.technofuturtic.tournament_api.dl.enums.TournamentFormat;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import jakarta.persistence.*;
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
    @Enumerated(EnumType.STRING)
    private TournamentFormat format;

    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TournamentStatus status;

    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ParticipantType participantType;

    @Getter @Setter
    @Column(nullable = false)
    private LocalDate startDate;

    @Getter @Setter
    @Column(nullable = false)
    private LocalDate endDate;

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
