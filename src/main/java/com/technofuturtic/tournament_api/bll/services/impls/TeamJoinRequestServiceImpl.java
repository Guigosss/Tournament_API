package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.team.responses.TeamJoinRequestResponse;
import com.technofuturtic.tournament_api.bll.exceptions.team.*;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.bll.services.TeamJoinRequestService;
import com.technofuturtic.tournament_api.dal.repositories.*;
import com.technofuturtic.tournament_api.dl.entities.*;
import com.technofuturtic.tournament_api.dl.enums.InvitationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamJoinRequestServiceImpl implements TeamJoinRequestService {
    private final TeamJoinRequestRepository requests;
    private final TeamRepository teams;
    private final UserRepository users;

    @Override
    @Transactional
    public TeamJoinRequestResponse submit(Integer teamId, Integer playerId) {
        UserEntity player = lockActivePlayer(playerId);
        TeamEntity team = lockTeam(teamId);
        checkCanJoin(team, playerId);
        if (requests.existsByTeamIdAndPlayerIdAndStatus(teamId, playerId, InvitationStatus.PENDING)) {
            throw new TeamConflictException("request", "Une demande est déjà en attente pour cette équipe.");
        }
        TeamJoinRequestEntity request = new TeamJoinRequestEntity();
        request.setTeam(team);
        request.setPlayer(player);
        return TeamJoinRequestResponse.fromRequest(requests.saveAndFlush(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamJoinRequestResponse> findMine(Integer playerId) {
        return responses(requests.findAllByPlayerIdOrderByCreatedAtDescIdDesc(playerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamJoinRequestResponse> findForTeam(Integer teamId, Integer actorId, boolean admin) {
        TeamEntity team = teams.findById(teamId).orElseThrow(() -> new TeamNotFoundException("Team not found"));
        checkCaptain(team, actorId, admin);
        return responses(requests.findAllByTeamIdOrderByCreatedAtDescIdDesc(teamId));
    }

    @Override
    @Transactional
    public TeamJoinRequestResponse accept(Integer requestId, Integer actorId, boolean admin) {
        TeamJoinRequestRepository.Target target = target(requestId);
        // Même ordre de verrouillage que la suppression du compte et les invitations.
        UserEntity player = lockActivePlayer(target.getPlayerId());
        TeamEntity team = lockTeam(target.getTeamId());
        checkCaptain(team, actorId, admin);
        TeamJoinRequestEntity request = lockPendingRequest(requestId);
        checkCanJoin(team, player.getId());
        team.addMember(player);
        teams.saveAndFlush(team);
        request.setStatus(InvitationStatus.ACCEPTED);
        return TeamJoinRequestResponse.fromRequest(requests.saveAndFlush(request));
    }

    @Override
    @Transactional
    public TeamJoinRequestResponse reject(Integer requestId, Integer actorId, boolean admin) {
        TeamJoinRequestRepository.Target target = target(requestId);
        TeamEntity team = lockTeam(target.getTeamId());
        checkCaptain(team, actorId, admin);
        TeamJoinRequestEntity request = lockPendingRequest(requestId);
        request.setStatus(InvitationStatus.REJECTED);
        return TeamJoinRequestResponse.fromRequest(requests.saveAndFlush(request));
    }

    @Override
    @Transactional
    public TeamJoinRequestResponse cancel(Integer requestId, Integer playerId) {
        TeamJoinRequestRepository.Target target = target(requestId);
        if (!playerId.equals(target.getPlayerId())) {
            throw new AccessDeniedException("Seul le demandeur peut annuler sa demande.");
        }
        lockTeam(target.getTeamId());
        TeamJoinRequestEntity request = lockPendingRequest(requestId);
        request.setStatus(InvitationStatus.CANCELLED);
        return TeamJoinRequestResponse.fromRequest(requests.saveAndFlush(request));
    }

    private UserEntity lockActivePlayer(Integer id) {
        UserEntity player = users.findByIdForUpdate(id).orElseThrow(() -> new UserNotFoundException("Player not found"));
        if (player.isDeleted()) throw new UserNotFoundException("Player not found");
        return player;
    }

    private TeamEntity lockTeam(Integer id) {
        return teams.findByIdForUpdate(id).orElseThrow(() -> new TeamNotFoundException("Team not found"));
    }

    private TeamJoinRequestRepository.Target target(Integer id) {
        return requests.findTargetById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande inexistante."));
    }

    private TeamJoinRequestEntity lockPendingRequest(Integer id) {
        TeamJoinRequestEntity request = requests.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande inexistante."));
        if (request.getStatus() != InvitationStatus.PENDING) {
            throw new TeamConflictException("request", "Cette demande n'est plus en attente.");
        }
        return request;
    }

    private void checkCaptain(TeamEntity team, Integer actorId, boolean admin) {
        if (!admin && (team.getCaptain() == null || !actorId.equals(team.getCaptain().getId()))) {
            throw new AccessDeniedException("Seul le capitaine ou un administrateur peut gérer ces demandes.");
        }
    }

    private void checkCanJoin(TeamEntity team, Integer playerId) {
        if (team.isArchived() || team.getCaptain() == null) {
            throw new TeamConflictException("team", "Cette équipe sans capitaine est conservée pour son historique.");
        }
        for (UserEntity member : team.getMembers()) {
            if (playerId.equals(member.getId())) {
                throw new TeamConflictException("player", "Le joueur est déjà membre de cette équipe.");
            }
        }
        if (team.getMembers().size() >= team.getTeamSize()) {
            throw new TeamConflictException("team", "L'équipe est complète.");
        }
    }

    private List<TeamJoinRequestResponse> responses(List<TeamJoinRequestEntity> entities) {
        List<TeamJoinRequestResponse> result = new ArrayList<>();
        for (TeamJoinRequestEntity request : entities) result.add(TeamJoinRequestResponse.fromRequest(request));
        return result;
    }
}