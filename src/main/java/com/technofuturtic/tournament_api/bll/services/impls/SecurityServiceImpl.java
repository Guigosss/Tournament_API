package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.bll.services.SecurityService;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private final TournamentRepository tournamentRepository;

    @Override
    public boolean isTournamentOrganizer(Integer tournamentId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserContext currentUser =
                (UserContext) authentication.getPrincipal();

        System.out.println("USER CONNECTE ID = " + currentUser.id());
        System.out.println("TOURNAMENT ID = " + tournamentId);

        boolean result = tournamentRepository.existsByIdAndOrganizer_Id(
                tournamentId,
                currentUser.id()
        );

        System.out.println("IS ORGANIZER = " + result);

        return result;
    }
}

