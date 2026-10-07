package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.exceptions.team.TeamConflictException;
import com.technofuturtic.tournament_api.bll.exceptions.team.TeamNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.bll.services.TeamMemberService;
import com.technofuturtic.tournament_api.bll.services.NotificationService;
import com.technofuturtic.tournament_api.dl.enums.NotificationType;
import com.technofuturtic.tournament_api.dal.repositories.TeamRepository;
import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamMemberServiceImpl implements TeamMemberService {
    private final TeamRepository teamRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void remove(Integer teamId, Integer playerId, Integer actorId, boolean admin) {
        TeamEntity team = findTeam(teamId);
        if (!admin && (team.getCaptain() == null || !actorId.equals(team.getCaptain().getId()))) {
            throw new AccessDeniedException("Only the captain or an administrator can remove a member");
        }
        UserEntity member = removeMember(team, playerId);
        notificationService.notify(member, NotificationType.MEMBER_REMOVED,
                teamId, "Vous avez été retiré de l'équipe " + team.getName() + ".");
    }

    @Override
    @Transactional
    public void leave(Integer teamId, Integer playerId) {
        TeamEntity team = findTeam(teamId);
        UserEntity member = removeMember(team, playerId);
        if (team.getCaptain() != null) {
            notificationService.notify(team.getCaptain(), NotificationType.MEMBER_LEFT,
                    teamId, member.getUsername() + " a quitté votre équipe " + team.getName() + ".");
        }
    }

    private TeamEntity findTeam(Integer teamId) {
        return teamRepository.findByIdForUpdate(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));
    }

    private UserEntity removeMember(TeamEntity team, Integer playerId) {
        if (team.getCaptain() != null && playerId.equals(team.getCaptain().getId())) {
            throw new TeamConflictException("captainId", "Transfer captaincy before removing or leaving as captain");
        }
        UserEntity selectedMember = null;
        for (UserEntity member : team.getMembers()) {
            if (playerId.equals(member.getId())) {
                selectedMember = member;
                break;
            }
        }
        if (selectedMember == null) {
            throw new UserNotFoundException("Player is not a member of this team");
        }
        team.getMembers().remove(selectedMember);
        teamRepository.saveAndFlush(team);
        return selectedMember;
    }
}