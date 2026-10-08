package com.technofuturtic.tournament_api.bll.generators;

import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.enums.PhaseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupStageGenerator implements MatchGenerator {

    @Override
    public PhaseType supportedType() {
        return PhaseType.GROUP_STAGE;
    }

    @Override
    public void generate(List<RoundEntity> rounds, List<ParticipantEntity> participants) {

    }
}
