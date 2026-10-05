package com.technofuturtic.tournament_api.bll.generators;

import com.technofuturtic.tournament_api.bll.exceptions.engine.match.TooManyParticipantsForBracketException;
import com.technofuturtic.tournament_api.dal.repositories.MatchRepository;
import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.enums.MatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class MatchGenerator {

    private final MatchRepository matchRepository;

    private final Random random = new Random();

    public void generateFirstRounds(RoundEntity round, List<ParticipantEntity> participants) {
        int bracketSize = round.getType().getBracketSize();
        int byeCount = calculateByeCount(bracketSize, participants.size());
        List<ParticipantEntity> slots = createFirstRoundSlots(participants, byeCount, bracketSize);
        saveMatches(round, slots);
    }

    public void generateEmpty(RoundEntity round) {
        int bracketSize = round.getType().getBracketSize();
        List<ParticipantEntity> slots = createEmptySlots(bracketSize);
        saveMatches(round, slots);
    }

    private void saveMatches(RoundEntity round, List<ParticipantEntity> slots) {
        matchRepository.saveAll(createMatches(round, slots));
    }

    private int calculateByeCount(int bracketSize, int participantCount) {
        int byeCount = bracketSize - participantCount;
        if (byeCount < 0) {
            throw new TooManyParticipantsForBracketException(participantCount, bracketSize);
        }
        return byeCount;
    }

    private List<ParticipantEntity> shuffleParticipants(List<ParticipantEntity> participants) {
        List<ParticipantEntity> shuffled = new ArrayList<>(participants);
        Collections.shuffle(shuffled, random);
        return shuffled;
    }

    private List<ParticipantEntity> createFirstRoundSlots(List<ParticipantEntity> participants, int byeCount, int bracketSize) {
        List<ParticipantEntity> shuffled = shuffleParticipants(participants);
        int matchCount = bracketSize / 2;

        //- Shuffle bye matches
        List<Boolean> byeMatches = new ArrayList<>();
        for (int i = 0; i < matchCount; i++) {
            byeMatches.add(i < byeCount);
        }
        Collections.shuffle(byeMatches, random);

        //- Generate bye
        List<ParticipantEntity> slots = new ArrayList<>(bracketSize);
        int participantIndex = 0;

        for (boolean hasBye : byeMatches) {
            slots.add(shuffled.get(participantIndex++));
            slots.add(hasBye ? null : shuffled.get(participantIndex++));
        }

        return slots;
    }

    private List<ParticipantEntity> createEmptySlots(int bracketSize) {
        return new ArrayList<>(Collections.nCopies(bracketSize, null));
    }

    private List<MatchEntity> createMatches(RoundEntity round, List<ParticipantEntity> slots) {
        List<MatchEntity> matches = new ArrayList<>();
        for (int i = 0; i < slots.size(); i+=2) {
            ParticipantEntity participant1 = slots.get(i);
            ParticipantEntity participant2 = slots.get(i + 1);
            MatchEntity match = createMatch(round, participant1, participant2);
            match.setOrderIndex((i / 2) + 1);
            matches.add(match);
        }

        return matches;
    }

    private MatchEntity createMatch(RoundEntity round, ParticipantEntity participant1, ParticipantEntity participant2) {
        MatchEntity match = new MatchEntity();
        match.setRound(round);
        match.setParticipant1(participant1);
        match.setParticipant2(participant2);
        match.setScore1(0);
        match.setScore2(0);
        if (participant1 == null && participant2 == null){
            match.setStatus(MatchStatus.WAITING);
        } else if (participant1 == null || participant2 == null) {
            match.setWinner(participant1 != null ? participant1 : participant2);
            match.setStatus(MatchStatus.BYE);
        } else {
            match.setStatus(MatchStatus.PENDING);
        }

        return match;
    }
}
