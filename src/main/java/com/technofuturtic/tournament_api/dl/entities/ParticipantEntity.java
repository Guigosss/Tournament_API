package com.technofuturtic.tournament_api.dl.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "participant", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tournament_id", "user_id"}),
        @UniqueConstraint(columnNames = {"tournament_id", "team_id"})
    })
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false) @ToString
public class ParticipantEntity {

    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private TournamentEntity tournament;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Getter @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private TeamEntity team;
}
