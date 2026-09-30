package com.technofuturtic.tournament_api.bll.generators;

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

    private static final int MIN_PARTICIPANTS = 2;
    private static final int MAX_PARTICIPANTS = 128;

    private final RoundRepository roundRepository;

    public void generate(PhaseEntity phase, int participantCount) {
        List<RoundEntity> rounds = switch (phase.getType()) {
            case ELIMINATION -> generateElimination(phase, participantCount);
            case GROUP_STAGE, LOSERS_BRACKET -> List.of(); // TODO
        };

        roundRepository.saveAll(rounds);
    }

    private List<RoundEntity> generateElimination(PhaseEntity phase, int participantCount) {
        if (participantCount < MIN_PARTICIPANTS || participantCount > MAX_PARTICIPANTS ) {
            throw new IllegalArgumentException("An elimination phase requires between %d and %d participants."
                    .formatted(MIN_PARTICIPANTS, MAX_PARTICIPANTS));
        }

        //- Next exponent 2
        int bracketSize = (int) Math.pow(2, Math.ceil(Math.log(participantCount) / Math.log(2)));

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

    private RoundEntity newRound(PhaseEntity phase, RoundType type, int orderIndex) {
        RoundEntity round = new RoundEntity();
        round.setType(type);
        round.setOrderIndex(orderIndex);
        round.setPhase(phase);
        return round;
    }
}
