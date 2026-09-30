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
        return List.of(newPhase(tournament, PhaseType.ELIMINATION, 1));
    }

    private List<PhaseEntity> generateDoubleElimination(TournamentEntity tournament) {
        return List.of(
                newPhase(tournament, PhaseType.ELIMINATION, 1),
                newPhase(tournament, PhaseType.LOSERS_BRACKET, 2)
        );
    }

    private List<PhaseEntity> generateGroupsThenPlayoff(TournamentEntity tournament) {
        return List.of(
                newPhase(tournament, PhaseType.GROUP_STAGE, 1),
                newPhase(tournament, PhaseType.ELIMINATION, 2)
        );
    }

    private PhaseEntity newPhase(TournamentEntity tournament, PhaseType type, int orderIndex) {
        PhaseEntity phase = new PhaseEntity();
        phase.setType(type);
        phase.setOrderIndex(orderIndex);
        phase.setTournament(tournament);

        return phase;
    }
}
