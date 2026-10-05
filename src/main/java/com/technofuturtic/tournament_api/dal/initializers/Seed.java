package com.technofuturtic.tournament_api.dal.initializers;

import com.technofuturtic.tournament_api.dal.repositories.ParticipantRepository;
import com.technofuturtic.tournament_api.dal.repositories.RoleRepository;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.RoleEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import com.technofuturtic.tournament_api.dl.enums.TournamentFormat;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Seed implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final TournamentRepository tournamentRepository;
    private final ParticipantRepository participantRepository;

    @Override
    public void run(String... args) throws Exception {

        if(userRepository.count() == 0) {
            RoleEntity userRole = new RoleEntity("user");
            RoleEntity adminRole = new RoleEntity("admin");

            roleRepository.save(userRole);
            roleRepository.save(adminRole);

            String emailUser = "user@test.be";
            String emailAdmin = "admin@test.be";

            String password = passwordEncoder.encode("Test1234=");

            List<UserEntity> users = List.of(
                    new UserEntity(
                            "user",
                            emailUser,
                            password,
                            userRole
                    ),
                    new UserEntity(
                            "admin",
                            emailAdmin,
                            password,
                            adminRole
                    )
            );

            userRepository.saveAll(users);

            //- Change this value for each test
            int participantCount = 57;

            //- Create tournament
            TournamentEntity tournament = new TournamentEntity();
            tournament.setName("Tournament Test");
            tournament.setDescription("Tournament generated for testing");
            tournament.setMaxParticipants(128);
            tournament.setFormat(TournamentFormat.SINGLE_ELIMINATION);
            tournament.setStatus(TournamentStatus.REGISTRATION_CLOSED);
            tournament.setStartDate(LocalDate.now());
            tournament.setRegistrationStartDate(LocalDate.now());
            tournament.setRegistrationEndDate(LocalDate.now());
            tournament.setOrganizer(users.get(1));
            tournament.setEndDate(LocalDate.now().plusMonths(2));
            tournament = tournamentRepository.save(tournament);

            //- Create participants
            for (int i = 0; i < participantCount; i++) {
                ParticipantEntity participant = new ParticipantEntity();
                participant.setTournament(tournament);
                participantRepository.save(participant);
            }
        }
    }
}