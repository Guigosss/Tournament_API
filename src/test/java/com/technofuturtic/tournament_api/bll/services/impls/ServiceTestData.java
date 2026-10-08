package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.dl.entities.*;
import org.springframework.test.util.ReflectionTestUtils;

final class ServiceTestData {
    private ServiceTestData() {}

    static UserEntity player(int id) {
        return new UserEntity(id, "player" + id, "p" + id + "@test.be", "hash", new RoleEntity("user"));
    }

    static TeamEntity team(int id, UserEntity captain) {
        TeamEntity team = new TeamEntity();
        ReflectionTestUtils.setField(team, "id", id);
        team.setName("Team" + id);
        team.setCaptain(captain);
        team.addMember(captain);
        return team;
    }
}
