package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.bll.services.CaptainService;
import com.technofuturtic.tournament_api.dal.repositories.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("captainService")
@RequiredArgsConstructor
public class CaptainServiceImpl implements CaptainService {

    private final TeamRepository teamRepository;

    @Override
    public boolean isTeamCaptain(Integer teamId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserContext currentUser =
                (UserContext) authentication.getPrincipal();

        return teamRepository.existsByIdAndCaptainId(
                teamId,
                currentUser.id()
        );
    }
}
