package com.technofuturtic.tournament_api.dl.entities;

import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "register_user",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tournament_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RegisterUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private TournamentEntity tournament;

    @Column(nullable = false)
    private LocalDateTime registerDate;

    @Getter @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;
}