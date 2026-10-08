package com.technofuturtic.tournament_api.bll.generators;

import com.technofuturtic.tournament_api.bll.exceptions.engine.round.EliminationParticipantCountOutOfRangeException;
import com.technofuturtic.tournament_api.bll.generators.rules.EliminationRules;
import com.technofuturtic.tournament_api.dal.repositories.RoundRepository;
import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.enums.RoundType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoundGenerator {

    private final RoundRepository roundRepository;

    public List<RoundEntity> generate(PhaseEntity phase, int participantCount) {
        validateParticipantCount(participantCount);

        List<RoundEntity> rounds = switch (phase.getType()) {

            case ELIMINATION -> generateElimination(phase, participantCount);

            case GROUP_STAGE -> List.of(); // TODO

            case LOSERS_BRACKET -> List.of(); // TODO
        };

        return roundRepository.saveAll(rounds);
    }

    private List<RoundEntity> generateElimination(PhaseEntity phase, int participantCount) {
        //- Next exponent 2
        int bracketSize = EliminationRules.bracketSize(participantCount);

        List<RoundType> types = new ArrayList<>();
        List<RoundEntity> rounds = new ArrayList<>();

        for (RoundType type : RoundType.values()) {
            if (type.getBracketSize() <= bracketSize) {
                types.add(type);
            }
        }

        for (int i = 0; i < types.size(); i++) {
            rounds.add(newRound(phase, types.get(i), i + 1));
        }

        return rounds;
    }

    private void validateParticipantCount(int participantCount) {
        if (participantCount < EliminationRules.MIN_PARTICIPANTS || participantCount > EliminationRules.MAX_PARTICIPANTS) {
            throw new EliminationParticipantCountOutOfRangeException(EliminationRules.MIN_PARTICIPANTS, EliminationRules.MAX_PARTICIPANTS);
        }
    }

    private RoundEntity newRound(PhaseEntity phase, RoundType type, int orderIndex) {
        RoundEntity round = new RoundEntity();
        round.setType(type);
        round.setOrderIndex(orderIndex);
        round.setPhase(phase);
        return round;
    }
}
