package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.generators.TournamentGenerator;
import com.technofuturtic.tournament_api.bll.services.TournamentEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TournamentEngineServiceImpl implements TournamentEngineService {

    private final TournamentGenerator tournamentGenerator;

    @Override
    public void generateTournament(Integer tournamentId) {
        tournamentGenerator.generate(tournamentId);
    }
}
