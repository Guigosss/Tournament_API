package com.technofuturtic.tournament_api.bll.generators;

import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.enums.PhaseType;

import java.util.List;

public interface MatchGenerator {

    PhaseType supportedType();

    void generate(List<RoundEntity> rounds, List<ParticipantEntity> participants);
}
