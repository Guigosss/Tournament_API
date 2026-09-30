package com.technofuturtic.tournament_api.bll.generators;

import com.technofuturtic.tournament_api.dal.repositories.PhaseRepository;
import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.enums.PhaseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PhaseGenerator {

    private final PhaseRepository phaseRepository;

    public List<PhaseEntity> generate(TournamentEntity tournament) {
        List <PhaseEntity> phases = switch (tournament.getFormat()) {

            case SINGLE_ELIMINATION -> generateSingleElimination(tournament);

            case DOUBLE_ELIMINATION -> generateDoubleElimination(tournament);

            case GROUPS_THEN_PLAYOFF -> generateGroupsThenPlayoff(tournament);
        };

        return phaseRepository.saveAll(phases);
    }

    private List<PhaseEntity> generateSingleElimination(TournamentEntity tournament) {
        PhaseEntity elimination = new PhaseEntity();
        elimination.setType(PhaseType.ELIMINATION);
        elimination.setOrderIndex(1);
        elimination.setTournament(tournament);

        return List.of(elimination);
    }

    private List<PhaseEntity> generateDoubleElimination(TournamentEntity tournament) {
        PhaseEntity elimination = new PhaseEntity();
        elimination.setType(PhaseType.ELIMINATION);
        elimination.setOrderIndex(1);
        elimination.setTournament(tournament);

        PhaseEntity losers = new PhaseEntity();
        losers.setType(PhaseType.LOSERS_BRACKET);
        losers.setOrderIndex(2);
        losers.setTournament(tournament);

        return List.of(elimination, losers);
    }

    private List<PhaseEntity> generateGroupsThenPlayoff(TournamentEntity tournament) {
        PhaseEntity groups = new PhaseEntity();
        groups.setType(PhaseType.GROUP_STAGE);
        groups.setOrderIndex(1);
        groups.setTournament(tournament);

        PhaseEntity playoffs = new PhaseEntity();
        playoffs.setType(PhaseType.ELIMINATION);
        playoffs.setOrderIndex(2);
        playoffs.setTournament(tournament);

        return List.of(groups, playoffs);
    }
}
