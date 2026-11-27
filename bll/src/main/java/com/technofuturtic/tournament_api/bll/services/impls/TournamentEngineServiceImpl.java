package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.generators.TournamentGenerator;
import com.technofuturtic.tournament_api.bll.metrics.TournamentMetrics;
import com.technofuturtic.tournament_api.bll.services.TournamentEngineService;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.enums.TournamentFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TournamentEngineServiceImpl implements TournamentEngineService {

    private final TournamentGenerator tournamentGenerator;
    private final TournamentRepository tournamentRepository;
    private final TournamentMetrics tournamentMetrics;

    @Override
    public void generateTournament(Integer tournamentId) {
        //- Format only used as a metric tag (null if the tournament does not exist: the generator throws)
        TournamentFormat format = tournamentRepository.findById(tournamentId)
                .map(TournamentEntity::getFormat)
                .orElse(null);

        tournamentMetrics.recordGeneration(format, () -> tournamentGenerator.generate(tournamentId));
    }
}
